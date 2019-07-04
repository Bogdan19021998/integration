package ai.distil.integration.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "cassandra")
public class CassandraConfig {
    //Wired up from the Spring boot configuration - via the yml file

    private List<String> servers = new ArrayList<>();
    private Map<String, Object> accountKeyspaceReplicationOptions = new LinkedHashMap<>();

    private String keyspace;

    public List<String> getServers() {

        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public Map<String, Object> getAccountKeyspaceReplicationOptions() {
        return accountKeyspaceReplicationOptions;
    }

    public void setAccountKeyspaceReplicationOptions(Map<String, Object> accountKeyspaceReplicationOptions) {
        this.accountKeyspaceReplicationOptions = accountKeyspaceReplicationOptions;
    }
}