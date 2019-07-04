package ai.distil.integration.job.sync.jdbc.vo.query.postgresql;

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
public class DefineSchemaQueryDefinitionPostgreSQL extends AbstractDefineSchemaQueryDefinition {
    private static final String DEFAULT_SQL_QUERY = "SELECT ordinal_position, " +
            "column_name, " +
            "data_type " +
            "FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE table_schema = ? " +
            "AND table_name = ? ";
    private String schema;
    private String tableName;

    public DefineSchemaQueryDefinitionPostgreSQL(String schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
    }

    @Override
    public List<Object> getQueryParams() {
        return Lists.newArrayList(schema, tableName);
    }

    @Override
    public ColumnDefinition mapResultSet(ResultSet resultSet) throws SQLException {
        int position = resultSet.getInt(1);
        String tableName = resultSet.getString(2);

        return new ColumnDefinition(position,
                tableName,
                DatasetColumnType.simplifyType(resultSet.getString(3)).mapToAttributeType()
        );
    }

    @Override
    public String getQuery() {
        return DEFAULT_SQL_QUERY;
    }
}
