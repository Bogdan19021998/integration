package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.tomcat.util.buf.StringUtils;
import org.asynchttpclient.Param;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MailChimpMembersWithSpecificFieldsRequest extends MailChimpMembersRequest {

    private List<String> fields;

    public MailChimpMembersWithSpecificFieldsRequest(String listId, String apiKey, DatasetPageRequest pageRequest, List<String> fields) {
        super(listId, apiKey, pageRequest);
        this.fields = fields;
    }

    @Override
    public List<Param> params() {
        List<Param> currentParams = super.params();
        if(fields.size() > 0) {
            currentParams.add(new Param("fields", StringUtils.join(fields, ',')));
        }

        return currentParams;
    }
}
