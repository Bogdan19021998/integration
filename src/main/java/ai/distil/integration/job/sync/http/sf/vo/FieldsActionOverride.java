package ai.distil.integration.job.sync.http.sf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldsActionOverride {
    private String formFactor;
    private Boolean isAvailableInTouch;
    private String name;
    private String pageId;
    private String url;
}
