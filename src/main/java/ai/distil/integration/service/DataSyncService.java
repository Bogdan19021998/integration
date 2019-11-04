package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.cassandra.repository.vo.IngestionResult;
import ai.distil.integration.constants.SyncErrors;
import ai.distil.integration.job.sync.AbstractConnection;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private static final int MAX_CONSECUTIVE_ERRORS_COUNT = 5;

    private final CassandraSyncRepository cassandraSyncRepository;
    private final DataSourceProxy dataSourceProxy;
    private final ConnectionFactory connectionFactory;
    private final SchemaSyncService schemaSyncService;

    public List<DTODataSource> findAllEligibleDataSources(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.getEligibleDataSources();
        } catch (Exception e) {
            log.error("Can't find all data sources.", e);
            return null;
        }
    }

    /**
     * @return new schema definition
     */
    public DataSourceDataHolder syncSchema(String tenantId, DataSourceDataHolder currentSchema, AbstractConnection connection) {

        log.debug("Syncing schema / Tenant: {} / DataSource ID: {} / Distil table name: {}", tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());
        cassandraSyncRepository.createTableIfNotExists(tenantId, currentSchema, true);

        DataSourceDataHolder newSchema = ParserFactory.buildParser(connection, currentSchema, ParserType.SIMPLE).refreshSchema();

        if(newSchema == null) {
            throw new IllegalStateException("Datasource is not accessible anymore");
        }

        List<AttributeChangeInfo> attributesChangeInfo = schemaSyncService.defineSchemaChanges(currentSchema, newSchema);
        attributesChangeInfo
                .forEach(attr -> cassandraSyncRepository.applySchemaChanges(tenantId, currentSchema.getDataSourceCassandraTableName(), attr));

        List<DTODataSourceAttribute> newAttributes = attributesChangeInfo.stream()
                .map(v -> {
                    if (v.getNewAttribute() == null) {
                        v.getOldAttribute().setVerifiedStillPresent(false);
                        return v.getOldAttribute();
                    } else {
                        v.getNewAttribute().setId(v.getAttributeId());
                        v.getNewAttribute().setSyncAttribute(Optional.ofNullable(v.getOldAttribute())
                                .map(DTODataSourceAttribute::getSyncAttribute)
                                .orElse(false));
                        return v.getNewAttribute();
                    }
                }).peek(attribute -> attribute.setDateLastVerified(new Date()))
                .collect(Collectors.toList());


        return new DataSourceDataHolder(newSchema.getDataSourceId(),
                currentSchema.getDataSourceCassandraTableName(),
                newAttributes,
                newSchema.getDataSourceType(),
                currentSchema.getDataSourceForeignKey());
    }

    public SyncProgressTrackingData reSyncDataSource(String tenantId, DataSourceDataHolder currentSchema, AbstractConnection connection) {
        DataSourceDataHolder updatedSchema = this.syncSchema(tenantId, currentSchema, connection);
        return syncDataSource(tenantId, updatedSchema, connection);
    }

    public SyncProgressTrackingData syncDataSource(String tenantId, DataSourceDataHolder currentSchema, AbstractConnection connection) {

        log.debug("Syncing DataSource / Tenant: {} / DataSource ID: {} / Distil Cassandra table name: {}", tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());

        AbstractParser parser = ParserFactory.buildParser(connection, currentSchema, ParserType.SIMPLE);

        Map<String, String> allExistingRows = cassandraSyncRepository.getAllRowsIdsAndHashes(tenantId, currentSchema);

        Set<String> existingPrimaryKeys = new HashSet<>(allExistingRows.size());

        int allRowsCountBeforeUpdate = allExistingRows.size();

        log.debug("Existing row count : {}", allRowsCountBeforeUpdate);

        ProgressAggregator progressAggregator = new ProgressAggregator();
        progressAggregator.startTracking();
        progressAggregator.setBeforeRowsCount(allRowsCountBeforeUpdate);

        parser.parse((holder, row) -> {
            IngestionResult ingestionResult = cassandraSyncRepository.insertWithStats(tenantId, currentSchema, row, allExistingRows, true);
//          remove the row from the all existing rows map, then left rows must be deleted after the sync
            allExistingRows.remove(ingestionResult.getPrimaryKey());
            progressAggregator.aggregate(ingestionResult, existingPrimaryKeys);
            existingPrimaryKeys.add(ingestionResult.getPrimaryKey());

            if (progressAggregator.getConsecutiveErrors() > MAX_CONSECUTIVE_ERRORS_COUNT) {
                throw new RuntimeException("Too many errors have occurred consecutively - aborting the sync");
            }

            if (progressAggregator.getSyncTrackingData().getProcessed() % 1000 == 0) {
                log.info("Processed {} records for Tenant: {} / DataSource ID: {} / Distil table name: {}", progressAggregator.getSyncTrackingData().getProcessed(), tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());
            }
        });

        log.info("Finished processing {} records for Tenant: {} / DataSource ID: {} / Distil table name: {}", progressAggregator.getSyncTrackingData().getProcessed(), tenantId, currentSchema.getDataSourceForeignKey(), currentSchema.getDataSourceCassandraTableName());

        if (allExistingRows.size() > 0) {
            log.debug("Deleting {} records from Cassandra that no longer exist in source", allExistingRows.size());

            allExistingRows.forEach((primaryKey, hash) ->
                    cassandraSyncRepository.deleteFromTable(tenantId, currentSchema, primaryKey, true));
        }

        progressAggregator.stopTracking();

        progressAggregator.setDeletedCount(allExistingRows.size());
        long uniqueRowsCount = existingPrimaryKeys.size();
        progressAggregator.setCurrentRowsCount(uniqueRowsCount);
        SyncProgressTrackingData trackingData = progressAggregator.getSyncTrackingData();

        log.debug("Saving sync tracking Data : {}", trackingData);
        saveDataSourceHistory(tenantId, currentSchema.getDataSourceForeignKey(), trackingData);

        return trackingData;
    }


    public void saveDataSourceHistory(String tenantId, Long dataSourceId, SyncProgressTrackingData trackingData) {

        log.debug("Saving data source history for data source {}", dataSourceId);

        boolean numberOfRecordsMatch = trackingData.getProcessed() == trackingData.getCurrentRowsCount();

        String notUniquePrimaryKeyError = numberOfRecordsMatch ? null : SyncErrors.NUMBER_OF_RECORDS_NOT_MATCH;

//        keep it like this, then we will be able to extend in case of adding new errors
        boolean hasError = !numberOfRecordsMatch;

        DataSourceHistory dataSourceHistory = new DataSourceHistory(
                null,
                dataSourceId,
                trackingData.getStartedDate(),
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
