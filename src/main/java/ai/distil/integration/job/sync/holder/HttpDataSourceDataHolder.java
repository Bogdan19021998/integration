package ai.distil.integration.job.sync.holder;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpDataSourceDataHolder {
    private String urlPart;
    private String httpMethod;

    private List<DTODataSourceAttribute> allAttributes;
}
