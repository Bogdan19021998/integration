package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.controller.dto.CommonConnectionRequest;
import ai.distil.integration.job.JobDefinitionEnum;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import ai.distil.integration.mapper.ConnectionMapper;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.integration.utils.RestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionFactory connectionFactory;
    private final DataSourceProxy dataSourceProxy;
    private final ConnectionMapper connectionMapper;
    private final ConnectionProxy connectionProxy;
    private final JobScheduler jobScheduler;
    private final CassandraSyncRepository cassandraSyncRepository;

    public Boolean checkConnectivity(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.isAvailable();
        } catch (Exception e) {
            log.error("Can't connect to the external datasource", e);
            return false;
        }
    }

    public void deleteConnectionData(CommonConnectionRequest request) {
        List<DTODataSource> allDataSource = dataSourceProxy.getAllDataSourcesByConnection(request.getTenantId(), request.getConnectionId());
        List<SyncDataSourceRequest> allDataSourcesJobs = allDataSource.stream()
                .map(d -> connectionMapper.toSyncDataSourceRequest(request, d.getId()))
                .collect(Collectors.toList());

//        delete all jobs
        jobScheduler.deleteJobs(JobDefinitionEnum.SYNC_CONNECTION, Collections.singletonList(connectionMapper.toSyncConnectionRequest(request)));
        jobScheduler.deleteJobs(JobDefinitionEnum.SYNC_DATASOURCE, allDataSourcesJobs);

//        delete all cassandra tables
        allDataSource.forEach(d -> cassandraSyncRepository.dropTableIfExists(request.getTenantId(), d.getDataSourceCassandraTableName()));

    }

    public List<DTODataSource> defineDatasource(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.getAllDataSources();
        } catch (Exception e) {
            log.error("Can't define data sources", e);
            return null;
        }
    }

    public boolean isConnectionDisabled(String tenantId, Long orgId, Long connectionId) {
        return !RestUtils.getBody(connectionProxy.findOnePrivate(tenantId, orgId, connectionId))
                .map(DTOConnection::getEnabled)
                .orElse(false);
    }
}
