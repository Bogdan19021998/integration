package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.integration.utils.RestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionFactory connectionFactory;
    private final ConnectionProxy connectionProxy;

    public Boolean checkConnectivity(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.isAvailable();
        } catch (Exception e) {
            log.error("Can't connect to the external datasource", e);
            return false;
        }
    }

    public List<DTODataSource> defineDatasource(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.getAllDataSources();
        } catch (Exception e) {
            log.error("Can't define data sources", e);
            return null;
        }
    }

    public Boolean isConnectionDisabled(String tenantId, Long orgId, Long connectionId) {
        return !RestUtils.getBody(connectionProxy.findOnePrivate(tenantId, orgId, connectionId))
                .map(DTOConnection::getEnabled)
                .orElse(false);
    }
}
