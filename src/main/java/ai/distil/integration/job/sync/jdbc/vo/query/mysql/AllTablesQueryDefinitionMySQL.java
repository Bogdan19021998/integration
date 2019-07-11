package ai.distil.integration.job.sync.jdbc.vo.query.mysql;

import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.job.sync.jdbc.vo.TableType;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractAllTablesQueryDefinition;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AllTablesQueryDefinitionMySQL extends AbstractAllTablesQueryDefinition {

    public static final String DEFAULT_SQL_QUERY = "SELECT table_name, table_type, table_comment FROM information_schema.tables WHERE table_schema = ?";
    private String schema;

    @Override
    public List<Object> getQueryParams() {
        return Lists.newArrayList(this.schema);
    }

    @Override
    public SimpleDataSourceDefinition mapResultSet(ResultSet resultSet) throws SQLException {
        String tableName = resultSet.getString(1);
        TableType tableType = getDataSourceType(resultSet.getString(2));
        String description = resultSet.getString(3);

        return new SimpleDataSourceDefinition(schema, tableName, tableType, description);
    }

    @Override
    public String getQuery() {
        return DEFAULT_SQL_QUERY;
    }

}
