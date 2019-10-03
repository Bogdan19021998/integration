package ai.distil.integration.job.sync.http.mailchimp.vo.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailChimpResponseError {
    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
}
