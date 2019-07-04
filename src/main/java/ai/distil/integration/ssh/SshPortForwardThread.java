package ai.distil.integration.ssh;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Optional;

@Slf4j
public class SshPortForwardThread extends Thread {

    private static final String LOCAL_ADDRESS = "localhost";

    private final SshPortForwardConfig portForwardConfig;
    private SSHClient sshClient;
    private ServerSocket listenSocket;

    public SshPortForwardThread(SshPortForwardConfig portForwardConfig) {
        this.portForwardConfig = portForwardConfig;
        setup();
    }

    private void setup() {
        try {
            sshClient = connectSsh();
            listenSocket = connectListenSocket();
        } catch (Exception e) {
            shutdown();
            throw new IllegalStateException(e);
        }
    }

    private void shutdown() {
        log.debug("SSH Forwarder - Shutting down SSH client");
        IOUtils.closeQuietly(sshClient);
        log.debug("SSH Forwarder - Shutting down socket");
        IOUtils.closeQuietly(listenSocket);
    }

    private SSHClient connectSsh() throws IOException {
        SshConnectionParameters connectionParameters = portForwardConfig.getConnectionParameters();

        // Use keep-alive so our connection doesn't reset
        DefaultConfig defaultConfig = buildDefaultConfig();
        SSHClient newClient = new SSHClient(defaultConfig);

        // Don't verify host keys as we won't have them imported (maybe a future feature?)
        newClient.addHostKeyVerifier(new PromiscuousVerifier());

        // Connect to SSH server ready to authenticate
        log.debug("Connecting to {}:{}", connectionParameters.getServerAddress(), connectionParameters.getServerPort());
        newClient.connect(connectionParameters.getServerAddress(), connectionParameters.getServerPort());

        // Authenticate
        if (connectionParameters instanceof KeySshConnectionParameters) {
            KeySshConnectionParameters keyConnectionParameters = (KeySshConnectionParameters) connectionParameters;

            KeyProvider key = newClient.loadKeys(keyConnectionParameters.getKey(), null, Optional.ofNullable(keyConnectionParameters.getKeyPassphrase())
                    .map(kp -> PasswordUtils.createOneOff(kp.toCharArray()))
                    .orElse(null));

            newClient.authPublickey(keyConnectionParameters.getUsername(), key);
        } else if (connectionParameters instanceof PasswordSshConnectionParameters) {
            PasswordSshConnectionParameters passwordConnectionParameters = (PasswordSshConnectionParameters) connectionParameters;

            newClient.authPassword(passwordConnectionParameters.getUsername(), passwordConnectionParameters.getPassword());
        }

        return newClient;
    }

    private DefaultConfig buildDefaultConfig() {
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        if (!log.isDebugEnabled()) {
            defaultConfig.setLoggerFactory(new net.schmizz.sshj.common.LoggerFactory() {
                public Logger getLogger(String name) {
                    return org.slf4j.helpers.NOPLogger.NOP_LOGGER;
                }

                public Logger getLogger(Class<?> clazz) {
                    return org.slf4j.helpers.NOPLogger.NOP_LOGGER;
                }
            });
        }
        return defaultConfig;
    }

    private ServerSocket connectListenSocket() throws IOException {
        ServerSocket listenSocket = new ServerSocket();
        listenSocket.setReuseAddress(true);
//      0 means that system will dedicate the free port
        listenSocket.bind(new InetSocketAddress(SshPortForwardThread.LOCAL_ADDRESS, 0));

        return listenSocket;
    }

    private LocalPortForwarder.Parameters makeParams(ServerSocket serverSocket) {
        return new LocalPortForwarder.Parameters(serverSocket.getLocalSocketAddress().toString(),
                serverSocket.getLocalPort(),
                portForwardConfig.getDestinationAddress(),
                portForwardConfig.getDestinationPort());
    }

    @Override
    public void run() {
        try {
            LocalPortForwarder.Parameters params = makeParams(this.listenSocket);
            log.debug("Listening on {}:{}", params.getLocalHost(), params.getLocalPort());

            // the following is blocking
            sshClient.newLocalPortForwarder(params, listenSocket).listen();

            log.debug("Forwarder for {} stopped", params.getLocalHost());
        } catch (Throwable e) {
            log.error("Forwarder for {} error", e);
        } finally {
            // always shutdown the client + socket
            shutdown();
        }
    }

    public int getListenPort() {
        return listenSocket.getLocalPort();
    }

    public String getListenAddress() {
        return LOCAL_ADDRESS;
    }

    @Override
    public void interrupt() {
        try {
            listenSocket.close();
        } catch (IOException ignored) {
            // we don't care
        }
        super.interrupt();
    }

}
