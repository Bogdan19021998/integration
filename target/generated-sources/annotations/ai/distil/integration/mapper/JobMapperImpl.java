package ai.distil.integration.mapper;

import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.integration.controller.dto.ScheduleConnectionSyncRequest;
import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.controller.dto.destination.SyncProgressTrackingData;
import ai.distil.integration.domain.SyncProgressTrackingDataEntity;
import ai.distil.integration.job.sync.request.SyncConnectionRequest;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import ai.distil.integration.job.sync.request.SyncDestinationRequest;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2020-01-02T14:32:30+0200",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 11.0.5 (Private Build)"
)
@Component
public class JobMapperImpl implements JobMapper {

    @Override
    public SyncProgressTrackingDataEntity mapTrackingData(SyncProgressTrackingData trackingData) {
        if ( trackingData == null ) {
            return null;
        }

        SyncProgressTrackingDataEntity syncProgressTrackingDataEntity = new SyncProgressTrackingDataEntity();

        syncProgressTrackingDataEntity.setCurrentTrackingTime( trackingData.getCurrentTrackingTime() );
        syncProgressTrackingDataEntity.setProcessed( trackingData.getProcessed() );
        syncProgressTrackingDataEntity.setCreated( trackingData.getCreated() );
        syncProgressTrackingDataEntity.setUpdated( trackingData.getUpdated() );
        syncProgressTrackingDataEntity.setDeleted( trackingData.getDeleted() );
        syncProgressTrackingDataEntity.setNotChanged( trackingData.getNotChanged() );
        syncProgressTrackingDataEntity.setCurrentRowsCount( trackingData.getCurrentRowsCount() );
        syncProgressTrackingDataEntity.setBeforeRowsCount( trackingData.getBeforeRowsCount() );
        syncProgressTrackingDataEntity.setStartedDate( trackingData.getStartedDate() );
        syncProgressTrackingDataEntity.setFinishedDate( trackingData.getFinishedDate() );

        return syncProgressTrackingDataEntity;
    }

    @Override
    public SyncDataSourceRequest mapSyncRequest(ScheduleDatasourceSyncRequest request) {
        if ( request == null ) {
            return null;
        }

        SyncDataSourceRequest syncDataSourceRequest = new SyncDataSourceRequest();

        syncDataSourceRequest.setOrgId( request.getOrgId() );
        syncDataSourceRequest.setTenantId( request.getTenantId() );
        syncDataSourceRequest.setConnectionId( request.getConnectionId() );
        syncDataSourceRequest.setDataSourceId( request.getDataSourceId() );

        return syncDataSourceRequest;
    }

    @Override
    public SyncConnectionRequest mapSyncConnectionRequest(ScheduleConnectionSyncRequest request) {
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
    public SyncDestinationRequest mapDestinationSyncRequest(BaseDestinationIntegrationRequest request) {
        if ( request == null ) {
            return null;
        }

        SyncDestinationRequest syncDestinationRequest = new SyncDestinationRequest();

        syncDestinationRequest.setOrgId( request.getOrgId() );
        syncDestinationRequest.setTenantId( request.getTenantId() );
        syncDestinationRequest.setIntegrationId( request.getIntegrationId() );

        return syncDestinationRequest;
    }
}
