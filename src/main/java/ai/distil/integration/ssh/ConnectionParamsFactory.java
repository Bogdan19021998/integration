package ai.distil.integration.ssh;

import ai.distil.model.org.ConnectionSettings;

public class ConnectionParamsFactory {

    public static SshConnectionParameters buildConnectionParams(ConnectionSettings connSettings) {
        if (connSettings.isSsh_enabled()) {
            if (connSettings.getSsh_key() != null) {
                return new KeySshConnectionParameters(connSettings.getSsh_serverAddress(), connSettings.getSsh_port(),
                        connSettings.getSsh_userName(), connSettings.getSsh_passphrase(), connSettings.getSsh_key());
            } else if (connSettings.getSsh_password() != null) {
                return new PasswordSshConnectionParameters(connSettings.getSsh_serverAddress(), connSettings.getSsh_port(),
                        connSettings.getSsh_userName(),
                        connSettings.getSsh_password());
            } else {
                return null;
            }
        }

        return null;
    }

}
