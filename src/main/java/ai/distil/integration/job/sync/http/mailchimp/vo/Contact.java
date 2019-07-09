package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Contact {
    private String company;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
}
