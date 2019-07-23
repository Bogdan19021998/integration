package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.cassandra.repository.vo.IngestionResult;
import ai.distil.integration.constants.SyncErrors;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.SyncTableDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.parser.AbstractParser;
import ai.distil.integration.job.sync.parser.ParserFactory;
import ai.distil.integration.job.sync.parser.ParserType;
import ai.distil.integration.job.sync.progress.ProgressAggregator;
import ai.distil.integration.job.sync.progress.SyncProgressTrackingData;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.integration.service.vo.AttributeChangeInfo;
import ai.distil.integration.utils.DateUtils;
import ai.distil.model.org.DataSourceHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final CassandraSyncRepository cassandraSyncRepository;
    private final DataSourceProxy dataSourceProxy;
    private final ConnectionFactory connectionFactory;
    private final SchemaSyncService schemaSyncService;


    public List<DTODataSource> findAllDataSources(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.getAllDataSources();
        } catch (Exception e) {
            log.error("Can't find all data sources.", e);
            return null;
        }
    }

    public List<DTODataSource> findAllEligibleDataSources(DTOConnection dtoConnection) {
        List<DTODataSource> allTables = findAllDataSources(dtoConnection);
        return filterEligibleDataSources(allTables);
    }

    public List<DTODataSource> filterEligibleDataSources(List<DTODataSource> dataSources) {
        Set<SyncTableDefinition> tablesDefinitions = Stream.of(SyncTableDefinition.values()).collect(Collectors.toSet());

        return dataSources.stream().filter(dataSource -> tablesDefinitions.stream()
                .anyMatch(tableDefinition -> tableDefinition.isTableNameFitNamingConvention(dataSource.getSourceTableName())))
                .collect(Collectors.toList());
    }

    /**
     * @return new schema definition
     */
    public DataSourceDataHolder syncSchema(String tenantId, DataSourceDataHolder currentSchema, AbstractConnection connection) {

        log.debug("Syncing schema / Tenant: {} / DataSource ID: {} / Distil table name: {}", tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());
        cassandraSyncRepository.createTableIfNotExists(tenantId, currentSchema, true);

        DataSourceDataHolder newSchema = ParserFactory.buildParser(connection, currentSchema, ParserType.SIMPLE).refreshSchema();
        newSchema.getAllAttributes().forEach(attribute -> attribute.setDateLastVerified(new Date()));

        List<AttributeChangeInfo> attributesChangeInfo = schemaSyncService.defineSchemaChanges(currentSchema, newSchema);
        attributesChangeInfo.forEach(attr -> cassandraSyncRepository.applySchemaChanges(tenantId, newSchema, attr));

        List<DTODataSourceAttribute> newAttributes = attributesChangeInfo.stream()
                .filter(attr -> attr.getNewAttribute() != null)
                .peek(v -> v.getNewAttribute().setId(v.getAttributeId()))
                .map(AttributeChangeInfo::getNewAttribute)
                .collect(Collectors.toList());


        return new DataSourceDataHolder(newSchema.getDataSourceId(),
                newSchema.getDataSourceCassandraTableName(),
                newAttributes,
                newSchema.getDataSourceType(),
                currentSchema.getDataSourceForeignKey());
    }

    public SyncProgressTrackingData reSyncDataSource(String tenantId, DataSourceDataHolder currentSchema, AbstractConnection connection) {

        log.debug("Re - Syncing DataSource / Tenant: {} / DataSource ID: {} / Distil Cassandra table name: {}", tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());

        DataSourceDataHolder updatedSchema = this.syncSchema(tenantId, currentSchema, connection);
        return syncDataSource(tenantId, updatedSchema, connection);
    }

    public SyncProgressTrackingData syncDataSource(String tenantId, DataSourceDataHolder currentSchema, AbstractConnection connection) {

        log.debug("Syncing DataSource / Tenant: {} / DataSource ID: {} / Distil Cassandra table name: {}", tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());

        AbstractParser parser = ParserFactory.buildParser(connection, currentSchema, ParserType.SIMPLE);

        Map<String, String> allExistingRows = cassandraSyncRepository.getAllRowsIdsAndHashes(tenantId, currentSchema);

        int allRowsCountBeforeUpdate = allExistingRows.size();

        log.debug("Existing row count : {}", allRowsCountBeforeUpdate);

        ProgressAggregator progressAggregator = new ProgressAggregator();
        progressAggregator.startTracking();
        progressAggregator.setBeforeRowsCount(allRowsCountBeforeUpdate);

        parser.parse((holder, row) -> {
            IngestionResult ingestionResult = cassandraSyncRepository.insertWithStats(tenantId, currentSchema, row, allExistingRows, true);
//          remove the row from the all existing rows map, then left rows must be deleted after the sync
            allExistingRows.remove(ingestionResult.getPrimaryKey());
            progressAggregator.aggregate(ingestionResult);

            if(progressAggregator.getConsecutiveErrors()>5){
                throw new RuntimeException("Too many errors have occurred consecutively - aborting the sync");
            }

            if(progressAggregator.getSyncTrackingData().getProcessed()%1000 == 0) {
                log.debug("Processed {} records for Tenant: {} / DataSource ID: {} / Distil table name: {}", progressAggregator.getSyncTrackingData().getProcessed(), tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());
            }
        });

        log.info("Finished processing {} records for Tenant: {} / DataSource ID: {} / Distil table name: {}", progressAggregator.getSyncTrackingData().getProcessed(), tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());

        if(allExistingRows.size()>0) {
            log.debug("Deleting {} records from Cassandra that no longer exist in source", allExistingRows.size());

            allExistingRows.forEach((primaryKey, hash) ->
                cassandraSyncRepository.deleteFromTable(tenantId, currentSchema, primaryKey, true));
        }

        log.trace("Stopping tacking");
        progressAggregator.stopTracking();

        log.trace("setDeletedCount");
        progressAggregator.setDeletedCount(allExistingRows.size());

        log.trace("Getting rows count");
        long rowsCount = cassandraSyncRepository.getRowsCount(tenantId, currentSchema);

        log.trace("Setting rows count");
        progressAggregator.setCurrentRowsCount(rowsCount);

        log.trace("Getting sync tracking data");
        SyncProgressTrackingData trackingData = progressAggregator.getSyncTrackingData();

        log.debug("Saving sync tracking Data : {}", trackingData);
        saveDataSourceHistory(tenantId, currentSchema.getDataSourceForeignKey(), trackingData);

        return trackingData;
    }


    public void saveDataSourceHistory(String tenantId, Long dataSourceId, SyncProgressTrackingData trackingData) {

        log.debug("Saving data source history");

        boolean numberOfRecordsMatch = trackingData.getProcessed() == trackingData.getCurrentRowsCount();

        String notUniquePrimaryKeyError = numberOfRecordsMatch ? null : SyncErrors.NUMBER_OF_RECORDS_NOT_MATCH;

//        keep it like this, then we will be able to extend in case of adding new errors
        boolean hasError = !numberOfRecordsMatch;

        DataSourceHistory dataSourceHistory = new DataSourceHistory(
                null,
                dataSourceId,
                DateUtils.toSqlDate(trackingData.getStartedDate()),
                trackingData.getCreated(),
                trackingData.getUpdated(),
                trackingData.getDeleted(),
                trackingData.getTaskDurationInSeconds(),
                hasError,
                notUniquePrimaryKeyError
        );

        dataSourceProxy.save(tenantId, dataSourceHistory);
    }

}
