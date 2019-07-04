package ai.distil.integration.job.sync.jdbc.vo.query.mssql;

import ai.distil.integration.job.sync.jdbc.vo.query.mysql.DefineSchemaQueryDefinitionMySQL;

public class DefineSchemaQueryDefinitionMsSQL extends DefineSchemaQueryDefinitionMySQL {
    public DefineSchemaQueryDefinitionMsSQL(String schema, String tableName) {
        super(schema, tableName);
    }
}
