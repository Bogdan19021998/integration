package ai.distil.integration.job.sync.http.sf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceDataPage {
    private Integer totalSize;
    private Boolean done;
    private String nextRecordsUrl;

    private List<Map<String, Object>> records;
}
