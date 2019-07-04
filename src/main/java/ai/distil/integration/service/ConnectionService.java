package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.service.sync.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionFactory connectionFactory;

    public Boolean checkConnectivity(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.isAvailable();
        } catch (Exception e) {
            log.debug("Can't connect to the external datasource", e);
            return false;
        }
    }

    public List<DTODataSource> defineDatasource(DTOConnection dtoConnection) {
        try (AbstractConnection abstractConnection = connectionFactory.buildConnection(dtoConnection)) {
            return abstractConnection.getAllDataSources();
        } catch (Exception e) {
            log.debug("Can't define data sources", e);
            return null;
        }
    }
}
