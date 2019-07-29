package ai.distil.integration.job;

import ai.distil.api.internal.controller.dto.UpdateConnectionDataRequest;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.job.sync.request.SyncConnectionRequest;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import ai.distil.integration.service.sync.ConnectionRequestMapper;
import ai.distil.model.types.ConnectionSchemaSyncStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;

@Slf4j
@Component
@DisallowConcurrentExecution
public class SyncConnectionJob extends QuartzJobBean {

    @Autowired
    private SyncDataSourceJob syncDataSourceJob;

    @Autowired
    private ConnectionProxy connectionProxy;

    @Autowired
    private DataSourceProxy dataSourceProxy;

    @Autowired
    private ConnectionRequestMapper requestMapper;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SyncConnectionRequest request = (SyncConnectionRequest) requestMapper.deserialize(jobExecutionContext.getMergedJobDataMap().getString(JOB_REQUEST),
                JobDefinitionEnum.SYNC_CONNECTION.getJobRequestClazz());

        List<DTODataSource> allDataSources = dataSourceProxy.getAllDataSourcesByConnection(request.getTenantId(), request.getConnectionId());

        connectionProxy.updateConnectionData(request.getTenantId(), request.getConnectionId(), new UpdateConnectionDataRequest(ConnectionSchemaSyncStatus.SYNC_IN_PROGRESS));

        boolean hasError = allDataSources.stream().map(dataSource -> {
            try {
                syncDataSourceJob.execute(new SyncDataSourceRequest(request.getOrgId(), request.getTenantId(), request.getConnectionId(), dataSource.getId()));
                return true;
            } catch (Exception e) {
                log.error("Can't sync datasource {}", dataSource.getId(), e);
                return false;
            }
        }).anyMatch(v -> !v);

        ConnectionSchemaSyncStatus syncResult = hasError ? ConnectionSchemaSyncStatus.LAST_SYNC_FAILED : ConnectionSchemaSyncStatus.SYNC_IN_PROGRESS;

        connectionProxy.updateConnectionData(request.getTenantId(), request.getConnectionId(), new UpdateConnectionDataRequest(syncResult));

    }
}
