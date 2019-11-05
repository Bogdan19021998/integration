package ai.distil.integration.service;

import ai.distil.processing.datapipeline.controller.proxy.DataPipelineClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataPipelineService {

    @Value("#{${ai.distil.datapipeline}}")
    private Map<String, String> tenantCodesByApiUrl;


    private final DataPipelineClient dataPipelineClient;

    public void resetDataPipelineForOrg(String tenantCode) {
        String url = tenantCodesByApiUrl.get(tenantCode);
        if (url == null) {
            log.warn("Url is not presented for tenant code - {}", tenantCode);
        } else {
            dataPipelineClient.resetCurrentExecution(url);
        }
    }
}
