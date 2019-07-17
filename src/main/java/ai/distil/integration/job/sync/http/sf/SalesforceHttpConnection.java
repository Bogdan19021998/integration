package ai.distil.integration.job.sync.http.sf;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.IFieldsHolder;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.sf.request.SalesforceLoginRequest;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceLoginResponse;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.service.RestService;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class SalesforceHttpConnection extends AbstractHttpConnection {

    public SalesforceHttpConnection(DTOConnection dtoConnection, RestService restService, IFieldsHolder fieldsHolder) {
        super(dtoConnection, restService, fieldsHolder);

    }

    //  todo consider refactoring for support connectable datasource, will be applicable for OAuth
    private void connect() {

    }

    @Override
    public List<DatasetRow> getNextPage(DataSourceDataHolder dataSource, PageRequest pageRequest) {
        return null;
    }

    @Override
    protected String getBaseUrl() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        SalesforceLoginRequest salesforceLoginRequest = new SalesforceLoginRequest(this.getConnectionSettings());
        SalesforceLoginResponse response = this.restService.execute(HttpConnectionConfiguration.SALESFORCE.getBaseUrl(), salesforceLoginRequest, JsonDataConverter.getInstance());
        return response != null && response.getAccessToken() != null;
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
