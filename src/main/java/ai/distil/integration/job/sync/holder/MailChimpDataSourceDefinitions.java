package ai.distil.integration.job.sync.holder;

import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Map;

public enum MailChimpDataSourceDefinitions implements IHttpSourceDefinition {
    CUSTOMER("/3.0/lists/%s/members", DataSourceType.CUSTOMER);

    private String urlPart;
    @Getter
    private DataSourceType dataSourceType;

    private static Map<DataSourceType, MailChimpDataSourceDefinitions> sourceDefinitionByType;

    static {
        MailChimpDataSourceDefinitions.sourceDefinitionByType = ListUtils.groupByWithOverwrite(
                Lists.newArrayList(MailChimpDataSourceDefinitions.values()),
                MailChimpDataSourceDefinitions::getDataSourceType,
                true);
    }

    MailChimpDataSourceDefinitions(String urlPart, DataSourceType dataSourceType) {
        this.urlPart = urlPart;
        this.dataSourceType = dataSourceType;
    }

    @Override
    public String urlPart(DTODataSource dataSource) {
//todo add client id
        return String.format(urlPart, dataSource.getSourceTableName());
    }

    public static IHttpSourceDefinition findSourceDefinition(DTODataSource dataSource) {
        return sourceDefinitionByType.get(dataSource.getDataSourceType());
    }
}
