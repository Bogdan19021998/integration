package ai.distil.integration.job.sync.http.request.mailchimp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.asynchttpclient.Param;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnyMailChimpAudienceRequest extends MailChimpAudiencesRequest {

    public AnyMailChimpAudienceRequest(String apiKey) {
        super(apiKey);
    }

    @Override
    public List<Param> params() {
        return buildDefaultPageParams(PageRequest.of(0, 1));
    }
}
