package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
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
    public DataSourceDataHolder updateSchemaIfChanged(Long orgId, DataSourceDataHolder currentSchema, AbstractConnection connection) {

        //TODO: NS - Added this here, as this was being called first in the chain as part of a new Sync, and the KeySpace / Table had not already been created
        //TODO: NS - refactor so we only call this once?
        cassandraSyncRepository.createTableIfNotExists(orgId, currentSchema, true);

        DataSourceDataHolder newSchema = ParserFactory.buildParser(connection, currentSchema, ParserType.SIMPLE).refreshSchema();
        newSchema.getAllAttributes().forEach(attribute -> attribute.setDateLastVerified(new Date()));

        List<AttributeChangeInfo> attributesChangeInfo = schemaSyncService.defineSchemaChanges(currentSchema, newSchema);
        attributesChangeInfo.forEach(attr -> cassandraSyncRepository.applySchemaChanges(orgId, newSchema, attr));

        return newSchema;
    }


    public SyncProgressTrackingData reSyncDataSource(Long orgId, DataSourceDataHolder currentSchema, AbstractConnection connection) {
        DataSourceDataHolder updatedSchema = this.updateSchemaIfChanged(orgId, currentSchema, connection);
        return syncDataSource(orgId, updatedSchema, connection);
    }

    public SyncProgressTrackingData syncDataSource(Long orgId, DataSourceDataHolder currentSchema, AbstractConnection connection) {

        AbstractParser parser = ParserFactory.buildParser(connection, currentSchema, ParserType.SIMPLE);

        cassandraSyncRepository.createTableIfNotExists(orgId, currentSchema, true);

        Map<String, String> allExistingRows = cassandraSyncRepository.getAllRowsIdsAndHashes(orgId, currentSchema);
        int allRowsCountBeforeUpdate = allExistingRows.size();

        ProgressAggregator progressAggregator = new ProgressAggregator();
        progressAggregator.startTracking();
        progressAggregator.setBeforeRowsCount(allRowsCountBeforeUpdate);

        parser.parse((holder, row) -> {
            IngestionResult ingestionResult = cassandraSyncRepository.insertWithStats(orgId, currentSchema, row, allExistingRows, true);
//          remove the row from the all existing rows map, then left rows must be deleted after the sync
            allExistingRows.remove(ingestionResult.getPrimaryKey());
            progressAggregator.aggregate(ingestionResult);
        });

        allExistingRows.forEach((primaryKey, hash) ->
                cassandraSyncRepository.deleteFromTable(orgId, currentSchema, primaryKey, true));

        progressAggregator.stopTracking();

        progressAggregator.setDeletedCount(allExistingRows.size());

        long rowsCount = cassandraSyncRepository.getRowsCount(orgId, currentSchema);
        progressAggregator.setCurrentRowsCount(rowsCount);

        SyncProgressTrackingData trackingData = progressAggregator.getSyncTrackingData();
        saveDataSourceHistory(trackingData);
        return trackingData;
    }


    public void saveDataSourceHistory(SyncProgressTrackingData trackingData) {
        boolean numberOfRecordsMatch = trackingData.getProcessed() == trackingData.getCurrentRowsCount();

        String notUniquePrimaryKeyError = numberOfRecordsMatch ? null : SyncErrors.NUMBER_OF_RECORDS_NOT_MATCH;

//        keep it like this, then we will be able to extend in case of adding new errors
        boolean hasError = !numberOfRecordsMatch;

        DataSourceHistory dataSourceHistory = new DataSourceHistory(
                null,
//                todo add datasource id if present
                null,
                DateUtils.toSqlDate(trackingData.getStartedDate()),
                trackingData.getCreated(),
                trackingData.getUpdated(),
                trackingData.getDeleted(),
                trackingData.getTaskDurationInSeconds(),
                hasError,
                notUniquePrimaryKeyError
        );

        dataSourceProxy.save(dataSourceHistory);
    }

}
