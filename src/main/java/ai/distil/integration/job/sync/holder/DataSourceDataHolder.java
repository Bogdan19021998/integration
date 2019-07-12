package ai.distil.integration.job.sync.holder;

import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class DataSourceDataHolder {
    @Getter
    private String distilTableName;

    @Getter
    private String sourceTableName;

    @Getter
    private List<DTODataSourceAttribute> attributesWithoutPrimaryKey;
    @Getter
    private List<DTODataSourceAttribute> allAttributes;

    @Getter
    private DTODataSourceAttribute primaryKey;

    public DataSourceDataHolder(String sourceTableName, String distilTableName, List<DTODataSourceAttribute> attributes) {
        this.distilTableName = distilTableName;
        this.sourceTableName = sourceTableName;

        this.allAttributes = attributes;
        this.attributesWithoutPrimaryKey = defineAttributesWithoutPrimaryKey(attributes);
        this.primaryKey = definePrimaryKey(attributes);
    }

    public static DataSourceDataHolder mapFromDTODataSourceEntity(DTODataSource dataSource) {
        return new DataSourceDataHolder(dataSource.getSourceTableName(), dataSource.getName(), dataSource.getAttributes());
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
