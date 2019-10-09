package ai.distil.integration.job;

import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.DestinationSourceProxy;
import ai.distil.integration.job.sync.request.SyncDestinationRequest;
import ai.distil.integration.service.sync.RequestMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;

@Slf4j
@Component
@DisallowConcurrentExecution
public class SyncDestinationIntegrationJob extends QuartzJobBean {

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private ConnectionProxy connectionProxy;

    @Autowired
    private DestinationSourceProxy destinationSourceProxy;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SyncDestinationRequest request = (SyncDestinationRequest) requestMapper.deserialize(jobExecutionContext.getMergedJobDataMap().getString(JOB_REQUEST),
                JobDefinitionEnum.SYNC_DESTINATION.getJobRequestClazz());

        DestinationIntegrationDTO integration = destinationSourceProxy.findOneById(request.getTenantId(), request.getIntegrationId()).getBody();




    }
}
