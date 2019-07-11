package ai.distil.integration.job.sync.jdbc.vo.query;

import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.job.sync.jdbc.vo.TableType;

public abstract class AbstractAllTablesQueryDefinition extends AbstractQueryDefinition<SimpleDataSourceDefinition> {

    protected TableType getDataSourceType(String value) {
        switch (value) {
            case "VIEW":
                return TableType.VIEW;
            case "BASE TABLE":
                return TableType.TABLE;
            default:
                throw new IllegalArgumentException(String.format("There is no source type %s supported", value));
        }
    }
}
