package ai.distil.integration.job.sync.holder;

import ai.distil.integration.utils.ListUtils;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Map;

public enum MailChimpDataSourceDefinition implements IHttpSourceDefinition {
    CUSTOMER("/lists/%s/members", DataSourceType.CUSTOMER);

    private String urlPart;
    @Getter
    private DataSourceType dataSourceType;

    private static Map<DataSourceType, MailChimpDataSourceDefinition> sourceDefinitionByType;

    static {
        MailChimpDataSourceDefinition.sourceDefinitionByType = ListUtils.groupByWithOverwrite(
                Lists.newArrayList(MailChimpDataSourceDefinition.values()),
                MailChimpDataSourceDefinition::getDataSourceType,
                true);
    }

    MailChimpDataSourceDefinition(String urlPart, DataSourceType dataSourceType) {
        this.urlPart = urlPart;
        this.dataSourceType = dataSourceType;
    }

    @Override
    public String urlPart(DataSourceDataHolder dataSource) {
        return String.format(urlPart, dataSource.getDataSourceId());
    }

    public static IHttpSourceDefinition findSourceDefinition(DataSourceDataHolder dataSource) {
        return sourceDefinitionByType.get(dataSource.getDataSourceType());
    }
}
