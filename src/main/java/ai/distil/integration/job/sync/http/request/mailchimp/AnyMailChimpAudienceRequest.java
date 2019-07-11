package ai.distil.integration.job.sync.http.request.mailchimp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.asynchttpclient.Param;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AnyMailChimpAudienceRequest extends MailChimpAudiencesRequest {

    @Override
    public List<Param> params() {
        return buildDefaultPageParams(PageRequest.of(0, 1));
    }
}
