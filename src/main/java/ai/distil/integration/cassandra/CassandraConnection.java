package ai.distil.integration.cassandra;

import ai.distil.integration.configuration.CassandraConfig;
import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.Map;

public class CassandraConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraConnection.class);
    private final CassandraConfig config;
    private Cluster cluster = null;
    private Session session = null;
    private Boolean connectionOk = false;
    private StatementCache statementCache;

    public CassandraConnection(Session session) {
        config = new CassandraConfig();

        this.cluster = session.getCluster();
        this.session = session.getCluster().connect("distil");

        statementCache = new StatementCache(this);
        CassandraMappings.map(this);

        connectionOk = true;
    }

    @Autowired
    public CassandraConnection(CassandraConfig config) {
        try {
            //Connect to Cassandra Cluster
            InetSocketAddress[] parsedServers = config.getServers().stream().map(s -> {
                String[] splitString = s.split(":", 2);

                if (splitString.length == 1) {
                    return new InetSocketAddress(s, 9042);
                } else {
                    return new InetSocketAddress(
                            splitString[0],
                            Integer.parseInt(splitString[1])
                    );
                }
            }).toArray(InetSocketAddress[]::new);

            LOGGER.debug("Setting Cassandra connection pooling options: Heartbeat : 300 seconds");

            PoolingOptions poolingOptions = new PoolingOptions();
            poolingOptions.setHeartbeatIntervalSeconds(300);

            cluster = Cluster.builder()
                    .withoutJMXReporting()
                    .addContactPointsWithPorts(parsedServers)
                    .withPoolingOptions(poolingOptions)
                    .build();

            if (LOGGER.isDebugEnabled()) {
                Metadata metadata = cluster.getMetadata();
                LOGGER.debug("Connected to cluster: {}", metadata.getClusterName());
                for (Host host : metadata.getAllHosts()) {
                    LOGGER.debug(
                            "Datacenter: {}; Host: {}; Rack: {}",
                            host.getDatacenter(),
                            host.getAddress(),
                            host.getRack());
                }
            }

            //Connect here - logging any exception
            session = cluster.connect(config.getKeyspace());

            statementCache = new StatementCache(this);
            CassandraMappings.map(this);

            this.config = config;
            connectionOk = true;
        } catch (Exception ex) {
            connectionOk = false;
            LOGGER.error("Error establishing connection to Cassandra Cluster.", ex);

            throw ex;
        }

        session.getCluster().register(new Host.StateListener() {
            private void hostHandler(Host host, boolean hostUp) {
                connectionOk = cluster.getMetadata().getAllHosts().stream()
                        .filter(h -> !h.equals(host))
                        .anyMatch(Host::isUp) || hostUp;
            }

            @Override
            public void onAdd(Host host) {
                hostHandler(host, true);
            }

            @Override
            public void onUp(Host host) {
                hostHandler(host, true);
            }

            @Override
            public void onDown(Host host) {
                hostHandler(host, false);
            }

            @Override
            public void onRemove(Host host) {
                hostHandler(host, false);
            }

            @Override
            public void onRegister(Cluster cluster) {
            }

            @Override
            public void onUnregister(Cluster cluster) {
                connectionOk = false;
            }
        });
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public String getKeyspace() {
        return session.getLoggedKeyspace();
    }

    public StatementCache getStatementCache() {
        return statementCache;
    }

    public Session getSession() {
        return session;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Boolean isConnectionOk() {
        return connectionOk;
    }

    public void registerCodec(TypeCodec<?> codec) {
        cluster.getConfiguration().getCodecRegistry().register(codec);
    }

    public Boolean testConnection() {
        try {
            getSession().execute("select release_version from system.local;");
            return true;
        } catch (Exception x) {
            LOGGER.error("Error testing Cassandra connection", x);
            return false;
        }
    }

    public Map<String, Object> getAccountKeyspaceReplicationOptions() {
        return config.getAccountKeyspaceReplicationOptions();
    }
}