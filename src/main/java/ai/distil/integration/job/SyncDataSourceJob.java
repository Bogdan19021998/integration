package ai.distil.integration.job;

import ai.distil.api.internal.controller.dto.DataSourceWithConnectionResponse;
import ai.distil.api.internal.controller.dto.UpdateConnectionDataRequest;
import ai.distil.api.internal.controller.dto.UpdateDataSourceDataRequest;
import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.api.internal.model.dto.newsfeed.SaveNewsfeedCardRequest;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.NewsfeedProxy;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.progress.SimpleSyncProgressListener;
import ai.distil.integration.job.sync.progress.SyncProgressTrackingData;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import ai.distil.integration.service.ConnectionService;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.integration.service.sync.RequestMapper;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.org.LastDataSourceSyncStatus;
import ai.distil.model.org.newsfeed.card.vo.datasource.DataSourceCardType;
import ai.distil.model.org.newsfeed.card.vo.datasource.DataSourceSyncDetails;
import ai.distil.model.org.newsfeed.card.vo.product.vo.StackCardDataSourceSyncCompletedAttributeInfo;
import ai.distil.model.types.ConnectionSchemaSyncStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;

@Slf4j
@Component
@DisallowConcurrentExecution
public class SyncDataSourceJob extends QuartzJobBean {

    private static final String DATA_SOURCE_LOG_KEY = "ds";

    @Autowired
    private SimpleSyncProgressListener syncProgressListener;

    @Autowired
    private ConnectionProxy connectionProxy;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private DataSyncService dataSyncService;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private NewsfeedProxy newsfeedProxy;

    @Autowired
    private RequestMapper requestMapper;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        SyncDataSourceRequest request = (SyncDataSourceRequest) requestMapper.deserialize(jobExecutionContext.getMergedJobDataMap().getString(JOB_REQUEST),
                JobDefinitionEnum.SYNC_DATASOURCE.getJobRequestClazz());

        if(connectionService.isConnectionDisabled(request.getTenantId(), request.getOrgId(), request.getConnectionId())) {
            log.info("Connection {} is disabled, skipping sync.", request.getConnectionId());
            return;
        }

        MDC.put(DATA_SOURCE_LOG_KEY, String.valueOf(request.getDataSourceId()));

        log.info("Starting sync job for datasource {}", request);

        updateConnectionStatus(request, ConnectionSchemaSyncStatus.SYNC_IN_PROGRESS);

        try {
            execute(request);
            updateConnectionStatus(request, ConnectionSchemaSyncStatus.SYNCED);
        } catch (Exception e) {
            log.error("Can't execute datasource - {} sync ", request.getDataSourceId(), e);
            updateConnectionStatus(request, ConnectionSchemaSyncStatus.LAST_SYNC_FAILED);
        } finally {
            log.info("Successfully finished sync job for datasource {}", request);
            MDC.clear();
        }
    }

    public void execute(SyncDataSourceRequest request) {

        ResponseEntity<DataSourceWithConnectionResponse> dataSourceResponse = connectionProxy.findOneDataSourcePrivate(request.getTenantId(), request.getOrgId(),
                request.getConnectionId(),
                request.getDataSourceId());

        if (dataSourceResponse.getStatusCode().isError() || dataSourceResponse.getBody() == null) {
            throw new IllegalArgumentException("There is no data source by this data or some internal error");
        }

        DTOConnection connectionDto = dataSourceResponse.getBody().getConnection();
        DTODataSource dataSourceDto = dataSourceResponse.getBody().getDataSource();

        //The dataSourceDto.getSyncTurnedOn() property can be NULL - After the initial Connection creation in order to prevent datasources from being
        //synced by default.  It is not set to false by default, as we want a way to tell if it is being enabled for the first time (i.e. from null to true)
        //to set off an immediate sync at that point.
        if(Boolean.FALSE.equals(connectionDto.getEnabled()) || dataSourceDto.getSyncTurnedOn() == null || Boolean.FALSE.equals(dataSourceDto.getSyncTurnedOn())) {
            log.info("Synchronisation for datasource - {} disabled, connection - {}, datasource sync - {} ",
                    dataSourceDto.getId(),
                    connectionDto.getEnabled(),
                    dataSourceDto.getSyncTurnedOn());

            return;
        }

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {

            DataSourceDataHolder dataSource = DataSourceDataHolder.mapFromDTODataSourceEntity(dataSourceDto);

            if (connection.dataSourceExist(dataSource)) {
                DataSourceDataHolder updatedSchema = dataSyncService.syncSchema(request.getTenantId(), dataSource, connection);

                connectionProxy.updateDataSourceData(request.getTenantId(), request.getDataSourceId(), new UpdateDataSourceDataRequest(null, updatedSchema.getSourceAttributes()));
                SyncProgressTrackingData syncResult = dataSyncService.syncDataSource(request.getTenantId(), updatedSchema, connection);

                createNewsfeedCard(request, connectionDto, dataSourceDto, updatedSchema, syncResult);

                List<DTODataSourceAttribute> attributes = this.updateDataSourceAttributesDataAfterSync(updatedSchema.getSourceAttributes(), syncResult);
                connectionProxy.updateDataSourceData(request.getTenantId(), request.getDataSourceId(), new UpdateDataSourceDataRequest(LastDataSourceSyncStatus.SUCCESS, attributes));

            } else {
                connectionProxy.updateDataSourceData(request.getTenantId(), request.getDataSourceId(), new UpdateDataSourceDataRequest(LastDataSourceSyncStatus.ERROR, null));
                throw new IllegalStateException("The data source no longer exists - updating the datasource details");
            }

        } catch (Exception e) {
            throw new RuntimeException("The error happened while running the job, do not rethrow exception because of retry policy", e);
        }

    }

    private void updateConnectionStatus(SyncDataSourceRequest request, ConnectionSchemaSyncStatus syncInProgress) {
        connectionProxy.updateConnectionData(request.getTenantId(), request.getConnectionId(), new UpdateConnectionDataRequest(syncInProgress));
    }

    private void createNewsfeedCard(SyncDataSourceRequest request, DTOConnection connection, DTODataSource dataSource, DataSourceDataHolder schema, SyncProgressTrackingData trackingData) {

        List<StackCardDataSourceSyncCompletedAttributeInfo> attributes = schema.getAllAttributes().stream()
                .map(attribute -> new StackCardDataSourceSyncCompletedAttributeInfo(attribute.getId(),
                        attribute.getAttributeDisplayName(), attribute.getAttributeDataTag()))
                .collect(Collectors.toList());

        SaveNewsfeedCardRequest saveRequest = new SaveNewsfeedCardRequest(dataSource.getId(), new DataSourceSyncDetails(
                connection.getId(),
                connection.getName(),
                dataSource.getId(),
                dataSource.getName(),
                trackingData.getProcessed(),
                trackingData.getTaskDurationInSeconds(),
                trackingData.getCreated(),
                trackingData.getDeleted(),
                trackingData.getUpdated(),
                attributes.size(),
                attributes,
                dataSource.getDataSourceType().toString()
        ), DataSourceCardType.DATA_SOURCE_SYNC_COMPLETED);

        newsfeedProxy.saveNewsfeedCardPrivate(saveRequest, request.getTenantId());
    }

//    warn impure, updating DTO here
    private List<DTODataSourceAttribute> updateDataSourceAttributesDataAfterSync(List<DTODataSourceAttribute> attributes, SyncProgressTrackingData trackingData) {
        Map<Long, DTODataSourceAttribute> attributesById = ListUtils.groupByWithOverwrite(attributes, DTODataSourceAttribute::getId, false);

        trackingData.getNotNullAttributeValues()
                .forEach((attributeId, notNullRowsCount) -> Optional.ofNullable(attributesById.get(attributeId))
                        .ifPresent((v) -> v.setNotNullValuesCount(notNullRowsCount)));

        return attributes;
    }

}
