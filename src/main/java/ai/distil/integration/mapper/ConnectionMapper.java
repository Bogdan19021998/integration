package ai.distil.integration.mapper;

import ai.distil.api.internal.model.dto.DTOConnection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConnectionMapper {
    DTOConnection copy(DTOConnection connection);
}
