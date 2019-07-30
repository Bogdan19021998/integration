package ai.distil.integration.job.sync.holder;

import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class DataSourceDataHolder {

    @Getter
    private String dataSourceCassandraTableName;

    @Getter
//  DataSource id in database
    private Long dataSourceForeignKey;

    @Getter
    private String dataSourceId;

    @Getter
    private DataSourceType dataSourceType;

    @Getter
    private List<DTODataSourceAttribute> attributesWithoutPrimaryKey;
    @Getter
    private List<DTODataSourceAttribute> allAttributes;

    @Getter
    private DTODataSourceAttribute primaryKey;

    public DataSourceDataHolder(String sourceTableName, String dataSourceCassandraTableName, List<DTODataSourceAttribute> attributes, DataSourceType dataSourceType, Long dataSourceForeignKey) {

        this.dataSourceId = sourceTableName;
        this.dataSourceCassandraTableName = dataSourceCassandraTableName;
        this.dataSourceType = dataSourceType;
        this.dataSourceForeignKey = dataSourceForeignKey;

        //filter out any customer_key attributes
        this.allAttributes = attributes.stream()
                .filter(attribute -> !attribute.getAttributeDistilName().endsWith("customer_key"))
                .filter(DTODataSourceAttribute::getVerifiedStillPresent)
                .collect(Collectors.toList());

        //Inferred
        this.attributesWithoutPrimaryKey = defineAttributesWithoutPrimaryKey(this.allAttributes);
        this.primaryKey = definePrimaryKey(this.allAttributes);
    }

    public static DataSourceDataHolder mapFromDTODataSourceEntity(DTODataSource dataSource) {
        return new DataSourceDataHolder(
                dataSource.getSourceTableName(),
                dataSource.getDataSourceCassandraTableName(),
                dataSource.getAttributes(),
                dataSource.getDataSourceType(),
                dataSource.getId());
    }

    private List<DTODataSourceAttribute> defineAttributesWithoutPrimaryKey(List<DTODataSourceAttribute> attributes) {
        return ImmutableList.copyOf(attributes
                .stream()
                .filter(DTODataSourceAttribute::getVerifiedStillPresent)
                .filter(attribute -> !attribute.getAttributeDataTag().isPrimaryKey())
                .collect(Collectors.toList()));
    }

    private DTODataSourceAttribute definePrimaryKey(List<DTODataSourceAttribute> attributes) {
//      todo make a decision about throwing the exception if there is no primary key
        return attributes.stream()
                .filter(attribute -> attribute.getAttributeDataTag() != null && attribute.getAttributeDataTag().isPrimaryKey())
                .findFirst()
                .orElse(null);
    }

}
