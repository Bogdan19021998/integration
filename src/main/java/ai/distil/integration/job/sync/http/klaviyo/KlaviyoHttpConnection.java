package ai.distil.integration.job.sync.http.klaviyo;


import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.request.mailchimp.AnyMailChimpAudienceRequest;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.service.RestService;

import java.util.List;

public class KlaviyoHttpConnection extends AbstractHttpConnection {

    private static final String DEFAULT_API_KEY_SEPARATOR = "-";

    private KlaviyoFieldsHolder fieldsHolder;

    private String baseUrl;

    public KlaviyoHttpConnection(DTOConnection dtoConnection, RestService restService, KlaviyoFieldsHolder klaviyoFieldsHolder) {
        super(dtoConnection, restService, klaviyoFieldsHolder);
        this.fieldsHolder = klaviyoFieldsHolder;

        String urlPart = "/api/v1/lists -G \\ -d api_key=pk_c573b131433429f49daef8ea6380147e97";
        //= ArrayUtils.get(1, getApiKey().split(DEFAULT_API_KEY_SEPARATOR))
        //        .orElseThrow(() -> new IllegalArgumentException("Api key is in invalid format."));

        this.baseUrl = String.format(HttpConnectionConfiguration.KLAVIYO.getBaseUrl(), urlPart);
    }


    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSource, DatasetPageRequest pageRequest) {

        return null;
    }

    @Override
    protected String getBaseUrl() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        AnyMailChimpAudienceRequest request = new AnyMailChimpAudienceRequest(getApiKey());
        AudiencesWrapper result = executeRequest(request);
        return result != null;
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        return null;
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition sourceDefinition) {

        return null;
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {

        return false;
    }
}
