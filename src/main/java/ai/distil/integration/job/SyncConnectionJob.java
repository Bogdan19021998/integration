package ai.distil.integration.job;

import ai.distil.api.internal.controller.dto.UpdateConnectionDataRequest;
import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.job.sync.request.SyncConnectionRequest;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import ai.distil.integration.service.ConnectionService;
import ai.distil.integration.service.DataPipelineService;
import ai.distil.integration.service.JobScheduler;
import ai.distil.integration.service.sync.RequestMapper;
import ai.distil.model.types.ConnectionSchemaSyncStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;

@Slf4j
@Component
@DisallowConcurrentExecution
public class SyncConnectionJob extends QuartzJobBean {

    private static String CONNECTION_LOG_KEY = "cn";
    private static String DATASOURCE_ID = "ds";

    @Autowired
    private SyncDataSourceJob syncDataSourceJob;

    @Autowired
    private ConnectionProxy connectionProxy;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private DataSourceProxy dataSourceProxy;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private DataPipelineService dataPipelineService;

    @Autowired
    private JobScheduler jobScheduler;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SyncConnectionRequest request = (SyncConnectionRequest) requestMapper.deserialize(jobExecutionContext.getMergedJobDataMap().getString(JOB_REQUEST),
                JobDefinitionEnum.SYNC_CONNECTION.getJobRequestClazz());


        Optional<DTOConnection> connection = connectionService.findConnection(request.getTenantId(), request.getOrgId(), request.getConnectionId());

        if(!connection.isPresent()) {
            jobScheduler.deleteJobs(JobDefinitionEnum.SYNC_CONNECTION, Collections.singleton(request));
            return;
        }

        if (!connection.map(DTOConnection::getEnabled).orElse(false)) {
            log.info("Connection {} is disabled, skipping sync.", request.getConnectionId());
            return;
        }

        MDC.put(CONNECTION_LOG_KEY, String.valueOf(request.getConnectionId()));

        log.info("Starting connection sync task for {} connection", request.getConnectionId());

        List<DTODataSource> allDataSources = dataSourceProxy.getAllDataSourcesByConnection(request.getTenantId(), request.getConnectionId());

        log.info("Starting syncing {} data sources", allDataSources.size());

        updateConnectionData(request, ConnectionSchemaSyncStatus.SYNC_IN_PROGRESS);

        boolean hasError = allDataSources.stream().map(dataSource -> {
            try {
                MDC.put(DATASOURCE_ID, String.valueOf(dataSource.getId()));
                SyncDataSourceRequest syncDsRequest = new SyncDataSourceRequest(request.getOrgId(), request.getTenantId(),
                        request.getConnectionId(), dataSource.getId());

                syncDataSourceJob.execute(syncDsRequest, true);
                return false;
            } catch (Exception e) {
                log.error("Can't sync datasource {}", dataSource.getId(), e);
                return true;
            }
        }).collect(Collectors.toList()).stream().anyMatch(v -> v);

        ConnectionSchemaSyncStatus syncResult = hasError ? ConnectionSchemaSyncStatus.LAST_SYNC_FAILED : ConnectionSchemaSyncStatus.SYNCED;

        log.info("Finished syncing {} data sources with a status {}", allDataSources.size(), syncResult);

        dataPipelineService.resetDataPipelineForOrg(request.getTenantId());

        updateConnectionData(request, syncResult);

    }

    private void updateConnectionData(SyncConnectionRequest request, ConnectionSchemaSyncStatus syncStatus) {
        connectionProxy.updateConnectionData(request.getTenantId(), request.getConnectionId(), new UpdateConnectionDataRequest(syncStatus, null));
    }
}
