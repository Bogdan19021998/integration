package ai.distil.integration.job.sync.jdbc.vo.query.redshift;

import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
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
public class AllTablesQueryDefinitionRedshiftSQL extends AbstractAllTablesQueryDefinition {

    public static final String DEFAULT_SQL_QUERY =
            " SELECT DISTINCT tablename " +
                    " FROM PG_TABLE_DEF " +
                    " WHERE schemaname = ?" +
                    " AND UPPER(tablename) LIKE '%DISTIL%'" +
                    " ORDER BY tablename ";

    private String schema;

    @Override
    public List<Object> getQueryParams() {
        return Lists.newArrayList(this.schema);
    }

    @Override
    public SimpleDataSourceDefinition mapResultSet(ResultSet resultSet) throws SQLException {
        String tableName = resultSet.getString(1);

        return new SimpleDataSourceDefinition(schema, tableName, null, null);
    }

    @Override
    public String getQuery() {
        return DEFAULT_SQL_QUERY;
    }

}
