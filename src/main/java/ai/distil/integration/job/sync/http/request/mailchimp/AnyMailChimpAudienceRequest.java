package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.asynchttpclient.Param;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnyMailChimpAudienceRequest extends MailChimpAudiencesRequest {

    public AnyMailChimpAudienceRequest(String apiKey) {
        super(apiKey);
    }

    @Override
    public List<Param> params() {
        return buildDefaultPageParams(new DatasetPageRequest(0, 1, null));
    }
}
