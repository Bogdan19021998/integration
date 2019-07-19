package ai.distil.integration.job.sync.http.sf.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordTypeInfo {
    private Boolean active;
    private Boolean available;

    private Boolean defaultRecordTypeMapping;
    private String developerName;
    private Boolean master;
    private String name;
    private String recordTypeId;
    private Map<String, Object> urls;
}
