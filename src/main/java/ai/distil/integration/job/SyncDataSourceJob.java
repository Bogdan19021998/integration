package ai.distil.integration.job;

import ai.distil.api.internal.controller.dto.DataSourceWithConnectionResponse;
import ai.distil.api.internal.controller.dto.UpdateConnectionDataRequest;
import ai.distil.api.internal.controller.dto.UpdateDataSourceDataRequest;
import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.progress.SimpleSyncProgressListener;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.integration.service.sync.ConnectionRequestMapper;
import ai.distil.model.org.LastDataSourceSyncStatus;
import ai.distil.model.types.ConnectionSchemaSyncStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;

@Slf4j
@DisallowConcurrentExecution
public class SyncDataSourceJob extends QuartzJobBean {

    @Autowired
    private SimpleSyncProgressListener syncProgressListener;

    @Autowired
    private ConnectionProxy connectionProxy;

    @Autowired
    private DataSyncService dataSyncService;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private ConnectionRequestMapper requestMapper;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        SyncDataSourceRequest request = (SyncDataSourceRequest) requestMapper.deserialize(jobExecutionContext.getMergedJobDataMap().getString(JOB_REQUEST),
                JobDefinitionEnum.SYNC_DATASOURCE
                        .getJobRequestClazz());

        ResponseEntity<DataSourceWithConnectionResponse> dataSourceResponse = connectionProxy.findOneDataSourcePrivate(request.getOrgId(),
                request.getConnectionId(),
                request.getDataSourceId());

        if (dataSourceResponse.getStatusCode().isError() || dataSourceResponse.getBody() == null) {
// todo think about raising an error in data sync history service
            log.warn("There is no data source by this data or some internal error: {}", dataSourceResponse);
// use plain return instead of an exception, because quartz will re-run the job
            return;
        }

        connectionProxy.updateConnectionData(request.getConnectionId(), new UpdateConnectionDataRequest(ConnectionSchemaSyncStatus.SYNC_IN_PROGRESS));

        DTOConnection connectionDto = dataSourceResponse.getBody().getConnection();
        DTODataSource dataSourceDto = dataSourceResponse.getBody().getDataSource();

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {

            DataSourceDataHolder dataSource = DataSourceDataHolder.mapFromDTODataSourceEntity(dataSourceDto);

            if (connection.dataSourceExist(dataSource)) {
                DataSourceDataHolder updatedSchema = dataSyncService.updateSchemaIfChanged(request.getOrgId(), dataSource, connection);

                //TODO: This is sending a list of attributes with blank IDs > causing the proxy function to fail.  It should be a mixture
                // of existing attributes that have changed (with ids) and new attributes to be created (without ids)
                connectionProxy.updateDataSourceData(request.getDataSourceId(), new UpdateDataSourceDataRequest(null, updatedSchema.getAllAttributes()));
                dataSyncService.syncDataSource(request.getOrgId(), updatedSchema, connection);
            } else {
                connectionProxy.updateDataSourceData(request.getDataSourceId(), new UpdateDataSourceDataRequest(LastDataSourceSyncStatus.ERROR, null));
                return;
            }


        } catch (Exception e) {
            connectionProxy.updateConnectionData(request.getConnectionId(), new UpdateConnectionDataRequest(ConnectionSchemaSyncStatus.LAST_SYNC_FAILED));
            log.error("The error happened while running the job, do not rethrow exception because of retry policy", e);
            return;
        }

        connectionProxy.updateConnectionData(request.getConnectionId(), new UpdateConnectionDataRequest(ConnectionSchemaSyncStatus.SYNCED));
        connectionProxy.updateDataSourceData(request.getDataSourceId(), new UpdateDataSourceDataRequest(LastDataSourceSyncStatus.SUCCESS, null));
    }

}
