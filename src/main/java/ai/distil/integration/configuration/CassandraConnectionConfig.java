package ai.distil.integration.configuration;

import ai.distil.integration.cassandra.CassandraConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConnectionConfig {
    @Bean
    @Autowired
    public CassandraConnection getCassandraConnection(CassandraConfig config) {
        return new CassandraConnection(config);
    }
}
