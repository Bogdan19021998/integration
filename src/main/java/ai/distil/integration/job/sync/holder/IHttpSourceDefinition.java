package ai.distil.integration.job.sync.holder;

import ai.distil.api.internal.model.dto.DTODataSource;
import org.asynchttpclient.Param;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IHttpSourceDefinition {

    String urlPart(DataSourceDataHolder dataSource);

    default List<Param> params(DTODataSource dataSource) {
        return Collections.emptyList();
    }

    default Map<String, Object> headers(DTODataSource dataSource) {
        return Collections.emptyMap();
    }

}
