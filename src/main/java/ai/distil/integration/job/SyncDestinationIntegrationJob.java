package ai.distil.integration.job;

import ai.distil.api.internal.model.dto.DestinationIntegrationSettingsDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.api.internal.proxy.DestinationSourceProxy;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.request.SyncDestinationRequest;
import ai.distil.integration.service.DestinationIntegrationService;
import ai.distil.integration.service.sync.RequestMapper;
import ai.distil.model.org.CustomerRecord;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;

@Slf4j
@Component
@DisallowConcurrentExecution
public class SyncDestinationIntegrationJob extends QuartzJobBean {

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private DestinationSourceProxy destinationSourceProxy;

    @Autowired
    private DestinationIntegrationService destinationIntegrationService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SyncDestinationRequest request = (SyncDestinationRequest) requestMapper.deserialize(jobExecutionContext.getMergedJobDataMap().getString(JOB_REQUEST),
                JobDefinitionEnum.SYNC_DESTINATION.getJobRequestClazz());

        DestinationIntegrationDTO integration = destinationSourceProxy.findOneByIdPrivate(request.getTenantId(), request.getIntegrationId()).getBody();

        AbstractDataSync dataSyncronizer = destinationIntegrationService.buildDataSync(request, integration);

        DestinationIntegrationSettingsDTO integrationSettings = dataSyncronizer.findIntegrationSettings();

        String listId = Optional.ofNullable(dataSyncronizer.createListIfNotExists())
                .orElseThrow(() -> new RuntimeException("Can't create list for the integration"));

        updateDestinationIntegrationData(request, integration, integrationSettings, listId);

        List<CustomAttributeDefinition> createdAttributes = dataSyncronizer.syncCustomAttributesSchema(listId);

        List<CustomerRecord> records = destinationSourceProxy.retrieveDestinationDataPrivate(request.getTenantId(), request.getIntegrationId()).getBody();
        dataSyncronizer.ingestData(listId, createdAttributes, records);

    }

    private void updateDestinationIntegrationData(SyncDestinationRequest request, DestinationIntegrationDTO integration, DestinationIntegrationSettingsDTO integrationSettings, String listId) {
        integration.setListId(listId);
        integration.setIntegrationSettings(integrationSettings);
        destinationSourceProxy.updateDestinationIntegrationPrivate(request.getTenantId(), integration);
    }
}
