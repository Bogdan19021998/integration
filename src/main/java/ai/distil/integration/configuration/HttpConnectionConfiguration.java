package ai.distil.integration.configuration;

import ai.distil.integration.configuration.vo.SalesforceHttpConnectionProps;
import ai.distil.integration.configuration.vo.SimpleHttpConnectionProps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
@ConfigurationProperties("ai.distil.integrations.connections.http")
public class HttpConnectionConfiguration {

    public static SimpleHttpConnectionProps MAIL_CHIMP;
    public static SimpleHttpConnectionProps CAMPAIGN_MONITOR;
    public static SimpleHttpConnectionProps KLAVIYO;
    public static SalesforceHttpConnectionProps SALESFORCE;

    public SimpleHttpConnectionProps mailChimp;
    public SimpleHttpConnectionProps campaignMonitor;
    public SalesforceHttpConnectionProps salesforce;
    public SimpleHttpConnectionProps klaviyo;


    @PostConstruct
    public void init() {
        MAIL_CHIMP = this.mailChimp;
        CAMPAIGN_MONITOR = this.campaignMonitor;
        SALESFORCE = this.salesforce;
        KLAVIYO = this.klaviyo;
    }

}
