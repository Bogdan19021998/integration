package ai.distil.integration.job.sync.jdbc.vo.query.mssql;

import ai.distil.integration.job.sync.jdbc.vo.query.postgresql.AllTablesQueryDefinitionPostgreSQL;

public class AllTablesQueryDefinitionMsSQL extends AllTablesQueryDefinitionPostgreSQL {
    public AllTablesQueryDefinitionMsSQL(String schema) {
        super(schema);
    }
}
