package ai.distil.integration.job.sync.jdbc.vo.query;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

public class SimpleCheckDataSourceExistingQueryDefinition extends AbstractQueryDefinition<Boolean> {

    private String schemaName;
    private String tableName;

    public SimpleCheckDataSourceExistingQueryDefinition(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    @Override
    public List<Object> getQueryParams() {
        return Collections.emptyList();
    }

    @Override
    public Boolean mapResultSet(ResultSet resultSet) {
        return true;
    }

    @Override
    public String getQuery() {
        return String.format("select * from %s.%s limit 0", schemaName, tableName);
    }
}
