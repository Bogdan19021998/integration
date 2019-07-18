package ai.distil.integration.job.sync.http.sf;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.sf.holder.SalesforceFieldsHolder;
import ai.distil.integration.job.sync.http.sf.request.SalesforceDataRequest;
import ai.distil.integration.job.sync.http.sf.request.SalesforceListFieldsRequest;
import ai.distil.integration.job.sync.http.sf.request.SalesforceLoginRequest;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceDataPage;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceListFields;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceLoginResponse;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.service.RestService;
import ai.distil.model.types.DataSourceType;

import java.util.List;
import java.util.stream.Collectors;

public class SalesforceHttpConnection extends AbstractHttpConnection {

    private SalesforceFieldsHolder fieldsHolder;
    private final String baseUrl;
    private final String apiVersion;
    private final String accessToken;

    public SalesforceHttpConnection(DTOConnection dtoConnection, RestService restService, SalesforceFieldsHolder fieldsHolder) {
        super(dtoConnection, restService, fieldsHolder);
        this.apiVersion = HttpConnectionConfiguration.SALESFORCE.getApiVersion();
        this.fieldsHolder = fieldsHolder;

        SalesforceLoginResponse response = connect();

        this.baseUrl = response.getInstanceUrl();
        this.accessToken = response.getAccessToken();
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSource, DatasetPageRequest pageRequest) {
        List<String> allFields = dataSource.getAllAttributes().stream()
                .filter(DTODataSourceAttribute::getSyncAttribute)
                .map(DTODataSourceAttribute::getAttributeSourceName)
                .collect(Collectors.toList());

        SalesforceDataRequest request = new SalesforceDataRequest(this.accessToken, this.apiVersion, allFields,
                dataSource.getDataSourceId(), pageRequest.getNextPageUrl());

        SalesforceDataPage dataPage = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());
        List<DatasetRow> rows = dataPage.getRecords().stream().map(this.fieldsHolder::transformRow).collect(Collectors.toList());

        return new DatasetPage(rows, dataPage.getNextRecordsUrl());
    }

    @Override
    protected int getDefaultPageNumber() {
        return 200;
    }

    @Override
    protected String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean isAvailable() {
        SalesforceLoginResponse loginResponse = connect();
        return loginResponse != null && loginResponse.getAccessToken() != null;
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        return this.fieldsHolder.getPredefinedDataSources()
                .stream()
                .map(this::buildDataSource)
                .collect(Collectors.toList());
    }

    private DTODataSource buildDataSource(SimpleDataSourceDefinition dataSource) {
        SalesforceListFieldsRequest request = new SalesforceListFieldsRequest(accessToken, apiVersion, dataSource.getDataSourceId());
        SalesforceListFields fields = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());
        return buildDataSource(dataSource, fields);
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition sourceDefinition) {
        return this.buildDataSource(sourceDefinition);
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        return false;
    }

    //  todo consider refactoring for support connectable datasource, will be applicable for OAuth
    private SalesforceLoginResponse connect() {
        SalesforceLoginRequest salesforceLoginRequest = new SalesforceLoginRequest(this.getConnectionSettings());
        return this.restService.execute(HttpConnectionConfiguration.SALESFORCE.getBaseUrl(), salesforceLoginRequest, JsonDataConverter.getInstance());
    }


    private DTODataSource buildDataSource(SimpleDataSourceDefinition dataSource, SalesforceListFields listFields) {
        return new DTODataSource(
                null,
                this.getConnectionData().getId(),
                generateTableName(dataSource.getDataSourceId()),
                null,
                dataSource.getDataSourceId(),
                null,
                null,
                null,
                DataSourceType.CUSTOMER,
                0,
                0,
                this.fieldsHolder.getAllFields(listFields.getFields()).stream().map(this::buildDTODataSourceAttribute).collect(Collectors.toList()),
                null
        );
    }
}
