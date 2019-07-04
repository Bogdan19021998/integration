package ai.distil.integration.service.vo;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeChangeInfo {
    private DTODataSourceAttribute oldAttribute;
    private DTODataSourceAttribute newAttribute;
    private AttributeChangeType attributeChangeType;
}
