package ai.distil.integration.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class StatementDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatementDefinition.class);

    private String statementName;

    private Function<String, BuiltStatement> statementFunction;

    public StatementDefinition(String statementName, Function<String, BuiltStatement> statementFunction) {
        this.statementName = statementName;
        this.statementFunction = statementFunction;
    }

    String getStatementName() {
        return statementName;
    }

    public PreparedStatement getStatement(CassandraConnection connection, String keyspace) {
        BuiltStatement statement = statementFunction.apply(keyspace);

        //        Lets log this for now
        //        if(LOGGER.isDebugEnabled())
        //        {
        assert statement != null;
        LOGGER.info("Preparing statement '{}:{}' - {}", keyspace, this.getStatementName(), statement.toString());
        //        }

        return connection.getSession().prepare(statement);
    }

    public BuiltStatement getStatementUnprepared(String keyspace) {
        return statementFunction.apply(keyspace);
    }
}