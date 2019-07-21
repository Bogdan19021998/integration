package ai.distil.integration.job.sync.jdbc.vo.query.postgresql;

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
public class AllTablesQueryDefinitionPostgreSQL extends AbstractAllTablesQueryDefinition {

    public static final String DEFAULT_SQL_QUERY = "SELECT table_name, table_type " +
            " FROM information_schema.tables " +
            " WHERE table_schema = ? " +
            " AND UPPER(table_name) like '%DISTIL%'" +
            " ORDER BY table_schema, table_name";
    private String schema;

    @Override
    public List<Object> getQueryParams() {
        return Lists.newArrayList(this.schema);
    }

    @Override
    public SimpleDataSourceDefinition mapResultSet(ResultSet resultSet) throws SQLException {
        String tableName = resultSet.getString(1);
        TableType tableType = getDataSourceType(resultSet.getString(2));

        return new SimpleDataSourceDefinition(schema, tableName, tableType, null);
    }

    @Override
    public String getQuery() {
        return DEFAULT_SQL_QUERY;
    }

}
