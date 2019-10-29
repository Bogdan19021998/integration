package ai.distil.integration.job.destination.vo;

import ai.distil.api.internal.model.dto.destination.HyperPersonalizedDestinationDTO;
import ai.distil.integration.utils.UrlWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UtmData {

    private String utmSourceText;
    private String utmMediumText;
    private String utmCampaignText;
    private String apiCode;

    private boolean hasData;

    public UtmData(HyperPersonalizedDestinationDTO destination) {
        this(Boolean.TRUE.equals(destination.getUtmSourceEnabled()) ? destination.getUtmSourceText() : null,
                Boolean.TRUE.equals(destination.getUtmMediumEnabled()) ? destination.getUtmMediumText() : null,
                Boolean.TRUE.equals(destination.getUtmCampaignEnabled()) ? destination.getUtmCampaignText() : null,
                destination.getApiCode());
    }

    public UtmData(String utmSourceText, String utmMediumText, String utmCampaignText, String apiCode) {
        this.utmSourceText = utmSourceText;
        this.utmMediumText = utmMediumText;
        this.utmCampaignText = utmCampaignText;
        this.apiCode = apiCode;

        this.hasData = utmSourceText != null || utmMediumText != null || utmCampaignText != null || apiCode != null;
    }

    public String fillUrl(String url) {

        if (url == null || !hasData) {
            return url;
        }

        return new UrlWrapper(url)
                .addParam("utm_source", utmSourceText)
                .addParam("utm_medium", utmMediumText)
                .addParam("utm_campaign", utmCampaignText)
                .addParam("DTC", apiCode)
                .getResultUrl();

    }


}
