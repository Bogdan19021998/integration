package ai.distil.integration.job.sync.jdbc.vo.query.redshift;

import ai.distil.integration.controller.dto.data.DatasetColumnType;
import ai.distil.integration.job.sync.jdbc.vo.ColumnDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractDefineSchemaQueryDefinition;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
public class DefineSchemaQueryDefinitionRedshiftSQL extends AbstractDefineSchemaQueryDefinition {
    private static final String DEFAULT_SQL_QUERY = "SELECT\n" +
            "       \"column\",\n" +
            "       type\n" +
            "FROM PG_TABLE_DEF\n" +
            "where schemaname = ? and tablename = ?";
    private String schema;
    private String tableName;

    public DefineSchemaQueryDefinitionRedshiftSQL(String schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
    }

    @Override
    public List<Object> getQueryParams() {
        return Lists.newArrayList(schema, tableName);
    }

    @Override
    public ColumnDefinition mapResultSet(ResultSet resultSet) throws SQLException {
        String fieldName = resultSet.getString(1);

        return new ColumnDefinition(null,
                fieldName,
                DatasetColumnType.simplifyType(resultSet.getString(2)).mapToAttributeType()
        );
    }

    @Override
    public String getQuery() {
        return DEFAULT_SQL_QUERY;
    }
}
