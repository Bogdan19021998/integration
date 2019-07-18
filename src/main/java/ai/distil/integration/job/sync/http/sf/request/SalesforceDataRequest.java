package ai.distil.integration.job.sync.http.sf.request;

import ai.distil.integration.job.sync.http.sf.vo.SalesforceDataPage;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.http.HttpMethod;

import java.util.List;

@Data
public class SalesforceDataRequest extends AbstractSalesforceRequest<SalesforceDataPage> {
    private List<String> fields;
    private String dataSourceId;

    public SalesforceDataRequest(String accessToken, String apiVersion, List<String> fields, String dataSourceId) {
        super(accessToken, apiVersion);
        this.fields = fields;
        this.dataSourceId = dataSourceId;
    }

    @Override
    public TypeReference<SalesforceDataPage> resultType() {
        return new TypeReference<SalesforceDataPage>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/services/data/v46.0/query/?q=select+%s+from+%s", StringUtils.join(fields, ','), dataSourceId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
