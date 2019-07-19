package ai.distil.integration.controller.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetPageRequest {
    private Integer pageNumber;
    private Integer pageSize;
    private String nextPageUrl;
}
