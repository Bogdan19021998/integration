package ai.distil.integration.job.sync.holder;

import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSourceDataHolder {
    @Getter
    private String distilTableName;

    @Getter
    private String dataSourceId;

    @Getter
    private DataSourceType dataSourceType;

    @Getter
    private List<DTODataSourceAttribute> attributesWithoutPrimaryKey;
    @Getter
    private List<DTODataSourceAttribute> allAttributes;

    private Map<String, DTODataSourceAttribute> attributesByDistilName;

    @Getter
    private DTODataSourceAttribute primaryKey;

    public DataSourceDataHolder(String sourceTableName, String distilTableName, List<DTODataSourceAttribute> attributes, DataSourceType dataSourceType) {
        this.distilTableName = distilTableName;
        this.dataSourceId = sourceTableName;

        this.allAttributes = attributes;
        this.attributesWithoutPrimaryKey = defineAttributesWithoutPrimaryKey(attributes);
        this.attributesByDistilName = ListUtils.groupByWithOverwrite(attributes, DTODataSourceAttribute::getAttributeDistilName, true);
        this.primaryKey = definePrimaryKey(attributes);
        this.dataSourceType = dataSourceType;
    }

    public static DataSourceDataHolder mapFromDTODataSourceEntity(DTODataSource dataSource) {
        return new DataSourceDataHolder(dataSource.getSourceTableName(), dataSource.getName(),
                dataSource.getAttributes(),
                dataSource.getDataSourceType());
    }

    public DTODataSourceAttribute getDataSourceAttribute(String alias) {
        return this.attributesByDistilName.get(alias);
    }

    private List<DTODataSourceAttribute> defineAttributesWithoutPrimaryKey(List<DTODataSourceAttribute> attributes) {
        return ImmutableList.copyOf(attributes.stream()
                .filter(attr -> !DataSourceSchemaAttributeTag.PRIMARY_KEY.equals(attr.getAttributeDataTag()))
                .collect(Collectors.toList()));
    }

    private DTODataSourceAttribute definePrimaryKey(List<DTODataSourceAttribute> attributes) {
//      todo make a decision about throwing the exception if there is no primary key
        return attributes.stream()
                .filter(attr -> DataSourceSchemaAttributeTag.PRIMARY_KEY.equals(attr.getAttributeDataTag()))
                .findFirst()
                .orElse(null);
    }

}
