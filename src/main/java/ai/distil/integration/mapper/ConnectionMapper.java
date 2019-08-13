package ai.distil.integration.mapper;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.controller.dto.CommonConnectionRequest;
import ai.distil.integration.job.sync.request.SyncConnectionRequest;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConnectionMapper {
    DTOConnection copy(DTOConnection connection);
    SyncConnectionRequest toSyncConnectionRequest(CommonConnectionRequest request);
    SyncDataSourceRequest toSyncDataSourceRequest(CommonConnectionRequest request, Long dataSourceId);
}
