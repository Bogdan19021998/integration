package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountInfo {

    private String accountId;
    private String loginId;
    private String accountName;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String avatarUrl;
    private String role;
    private String memberSince;
    private String pricingPlanType;
    private String firstPayment;
    private String accountTimezone;
    private String accountIndustry;
    private Contact contact;
    private String company;
    private String addr1;
    private String addr2;
    private String city;
    private String state;
    private String zip;
    private String country;

    private Boolean proEnabled;
    private String lastLogin;
    private Long totalSubscribers;

    @JsonProperty("_links")
    private List<Link> links;

}
