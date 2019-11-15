package ai.distil.integration.job.sync.holder;

import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.job.sync.SyncTableDefinition;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataSourceDataHolder {

    @Getter
    private String dataSourceCassandraTableName;

    private SyncTableDefinition syncTableDefinition;

    @Getter
//  DataSource id in database
    private Long dataSourceForeignKey;

    @Getter
    private String dataSourceId;

    @Getter
    private DataSourceType dataSourceType;

    @Getter
    private List<DTODataSourceAttribute> attributesWithoutPrimaryKey;

    //  all attributes filtered
    @Getter
    private List<DTODataSourceAttribute> allAttributes;

    //  all attributes without filtering
    @Getter
    private List<DTODataSourceAttribute> sourceAttributes;

    @Getter
    private DTODataSourceAttribute primaryKey;

    public DataSourceDataHolder(String sourceTableName, String dataSourceCassandraTableName, List<DTODataSourceAttribute> attributes, DataSourceType dataSourceType, Long dataSourceForeignKey) {

        this.dataSourceId = sourceTableName;
        this.dataSourceCassandraTableName = dataSourceCassandraTableName;
        this.dataSourceType = dataSourceType;
        this.dataSourceForeignKey = dataSourceForeignKey;

        this.sourceAttributes = attributes;

        //filter out any customer_key attributes
        this.allAttributes = attributes.stream()
                .filter(attribute -> !attribute.getAttributeDistilName().endsWith("customer_key"))
                .filter(DTODataSourceAttribute::getVerifiedStillPresent)
                .collect(Collectors.toList());

        this.syncTableDefinition = SyncTableDefinition.defineSyncTableDefinition(dataSourceType)
                .orElseThrow(() -> new RuntimeException(String.format("Unable to recognize datasource type - %s", dataSourceType)));

        //Inferred
        this.primaryKey = definePrimaryKey(this.allAttributes);
        this.attributesWithoutPrimaryKey = defineAttributesWithoutPrimaryKey(this.allAttributes);
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
        String attrDistilName = Optional.ofNullable(this.primaryKey).map(DTODataSourceAttribute::getAttributeDistilName).orElse(null);

        return ImmutableList.copyOf(attributes
                .stream()
                .filter(DTODataSourceAttribute::getVerifiedStillPresent)
                .filter(attribute -> !attribute.getAttributeDistilName().equalsIgnoreCase(attrDistilName))
                .collect(Collectors.toList()));
    }

    private DTODataSourceAttribute definePrimaryKey(List<DTODataSourceAttribute> attributes) {
        return attributes.stream()
                .filter(attribute -> this.syncTableDefinition.isPrimaryKey(attribute.getAttributeDataTag()))
                .sorted(Comparator.comparingInt(value -> this.syncTableDefinition.getPrimaryKeyPriority(value.getAttributeSourceName(), value.getAttributeDataTag())))
                .findFirst()
                .orElse(null);
    }

}
