package ai.distil.integration.job.sync.http.sf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SalesforceLoginResponse extends AbstractSalesforceRequest {
   private String accessToken;
   private String instanceUrl;
   private String id;
   private String tokenType;
   private String issuedAt;
   private String signature;
}
