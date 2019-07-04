package ai.distil.integration.job.sync.jdbc.vo.query;

import lombok.AllArgsConstructor;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SimpleCheckDataSourceExistingQueryDefinition extends AbstractQueryDefinition<Boolean> {
    private String tableName;

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
        return String.format("select * from %s limit 0", tableName);
    }
}
