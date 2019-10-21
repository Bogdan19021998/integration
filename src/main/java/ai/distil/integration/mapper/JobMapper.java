package ai.distil.integration.mapper;

import ai.distil.integration.controller.dto.ScheduleConnectionSyncRequest;
import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.integration.domain.SyncProgressTrackingDataEntity;
import ai.distil.integration.job.sync.progress.SyncProgressTrackingData;
import ai.distil.integration.job.sync.request.SyncConnectionRequest;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import ai.distil.integration.job.sync.request.SyncDestinationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mappings({
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "jobId", ignore = true),
    })
    SyncProgressTrackingDataEntity mapTrackingData(SyncProgressTrackingData trackingData);

    SyncDataSourceRequest mapSyncRequest(ScheduleDatasourceSyncRequest request);

    SyncConnectionRequest mapSyncConnectionRequest(ScheduleConnectionSyncRequest request);

    SyncDestinationRequest mapDestinationSyncRequest(BaseDestinationIntegrationRequest request);

}


