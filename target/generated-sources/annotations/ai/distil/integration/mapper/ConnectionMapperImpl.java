package ai.distil.integration.mapper;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.controller.dto.CommonConnectionRequest;
import ai.distil.integration.job.sync.request.SyncConnectionRequest;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2020-01-02T14:32:30+0200",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 11.0.5 (Private Build)"
)
@Component
public class ConnectionMapperImpl implements ConnectionMapper {

    @Override
    public DTOConnection copy(DTOConnection connection) {
        if ( connection == null ) {
            return null;
        }

        DTOConnection dTOConnection = new DTOConnection();

        dTOConnection.setId( connection.getId() );
        dTOConnection.setName( connection.getName() );
        dTOConnection.setDescription( connection.getDescription() );
        dTOConnection.setIsDatabase( connection.getIsDatabase() );
        dTOConnection.setConnectionType( connection.getConnectionType() );
        dTOConnection.setConnectionSettings( connection.getConnectionSettings() );
        dTOConnection.setLastSavedByUser( connection.getLastSavedByUser() );
        dTOConnection.setDateSaved( connection.getDateSaved() );
        dTOConnection.setNumDataSources( connection.getNumDataSources() );
        dTOConnection.setNumAudiences( connection.getNumAudiences() );
        dTOConnection.setSchemaSyncStatus( connection.getSchemaSyncStatus() );
        dTOConnection.setIntegrationSettings( connection.getIntegrationSettings() );
        dTOConnection.setDateLastSchemaSync( connection.getDateLastSchemaSync() );
        dTOConnection.setEnabled( connection.getEnabled() );
        dTOConnection.setConnectionEstablished( connection.getConnectionEstablished() );

        return dTOConnection;
    }

    @Override
    public SyncConnectionRequest toSyncConnectionRequest(CommonConnectionRequest request) {
        if ( request == null ) {
            return null;
        }

        SyncConnectionRequest syncConnectionRequest = new SyncConnectionRequest();

        syncConnectionRequest.setOrgId( request.getOrgId() );
        syncConnectionRequest.setTenantId( request.getTenantId() );
        syncConnectionRequest.setConnectionId( request.getConnectionId() );

        return syncConnectionRequest;
    }

    @Override
    public SyncDataSourceRequest toSyncDataSourceRequest(CommonConnectionRequest request, Long dataSourceId) {
        if ( request == null && dataSourceId == null ) {
            return null;
        }

        SyncDataSourceRequest syncDataSourceRequest = new SyncDataSourceRequest();

        if ( request != null ) {
            syncDataSourceRequest.setOrgId( request.getOrgId() );
            syncDataSourceRequest.setTenantId( request.getTenantId() );
            syncDataSourceRequest.setConnectionId( request.getConnectionId() );
        }
        if ( dataSourceId != null ) {
            syncDataSourceRequest.setDataSourceId( dataSourceId );
        }

        return syncDataSourceRequest;
    }
}
