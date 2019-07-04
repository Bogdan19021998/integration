package ai.distil.integration.cassandra;

import com.datastax.driver.core.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StatementCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatementCache.class);
    private final ConcurrentMap<String, PreparedStatement> statementMap = new ConcurrentHashMap<>();
    private CassandraConnection connection;

    public StatementCache(CassandraConnection connection) {
        this.connection = connection;
    }

    private String getKeyForStatement(String keyspace, StatementDefinition statement) {
        return String.format("%s:%s", keyspace, statement.getStatementName());
    }

    public PreparedStatement getOrAdd(String keyspace, StatementDefinition statement) {
        String key = getKeyForStatement(keyspace, statement);

        return statementMap.computeIfAbsent(
                key,
                (k) -> statement.getStatement(connection, keyspace)
        );
    }

    public void invalidateStatement(String keyspace, StatementDefinition statement) {
        String key = getKeyForStatement(keyspace, statement);
        statementMap.remove(key);
    }
}