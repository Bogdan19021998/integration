package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.integration.job.sync.http.request.mailchimp.*;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.ArrayUtils;
import ai.distil.integration.utils.ConcurrentUtils;
import ai.distil.model.types.DataSourceType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MailChimpHttpConnection extends AbstractHttpConnection {

    private static final String DEFAULT_API_KEY_SEPARATOR = "-";

    private MailChimpMembersFieldsHolder fieldsHolder;
    private String baseUrl;

    public MailChimpHttpConnection(DTOConnection dtoConnection, RestService restService, MailChimpMembersFieldsHolder fieldsHolder) {
        super(dtoConnection, restService, fieldsHolder);
        this.fieldsHolder = fieldsHolder;

        String urlPart = ArrayUtils.get(1, getApiKey().split(DEFAULT_API_KEY_SEPARATOR))
                .orElseThrow(() -> new IllegalArgumentException("Api key is in invalid format."));
        this.baseUrl = String.format(HttpConnectionConfiguration.MAIL_CHIMP.getBaseUrl(), urlPart);
    }

    @Override
    public boolean isAvailable() {
        AnyMailChimpAudienceRequest request = new AnyMailChimpAudienceRequest(getApiKey());
        AudiencesWrapper result = executeRequest(request);
        return result != null;
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        AudiencesWrapper result = executeRequest(new MailChimpAudiencesRequest(getApiKey()));
        return buildMultipleDataSources(result.getList());
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition dataSource) {
        return Optional.ofNullable(executeRequest(
                new SingleMailChimpAudienceRequest(getApiKey(), dataSource.getDataSourceId())))
                .map(audience -> buildDataSource(audience, getDataSourceAttributes(audience.getId())))
                .orElse(null);
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        SingleMailChimpAudienceRequest request = new SingleMailChimpAudienceRequest(getApiKey(), dataSource.getDataSourceId());
        Audience result = executeRequest(request);
        return result != null;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSourceHolder, DatasetPageRequest pageRequest) {
        MailChimpMembersRequest request = new MailChimpMembersRequest(dataSourceHolder.getDataSourceId(), getApiKey(), pageRequest);
        MembersWrapper response = executeRequest(request);

        return new DatasetPage(response.getMembers()
                .stream()
                .map(row -> fieldsHolder.transformRow(row, dataSourceHolder))
                .collect(Collectors.toList()), null);
    }

    @Override
    protected String getBaseUrl() {
        return this.baseUrl;
    }

    private List<DTODataSourceAttribute> getDataSourceAttributes(String listId) {
        MailChimpMergeFieldsRequest mergeFieldsRequest = new MailChimpMergeFieldsRequest(getApiKey(), listId);

        Map<String, Object> mergeFieldsDefinition = executeRequest(mergeFieldsRequest);

        return getDataSourceAttributes(mergeFieldsDefinition);
    }

    private List<DTODataSourceAttribute> getDataSourceAttributes(Map<String, Object> fieldsDefinition) {
        return this.fieldsHolder.getAllFields(Collections.singletonList(fieldsDefinition))
                .stream()
                .map(this::buildDTODataSourceAttribute)
                .collect(Collectors.toList());
    }

    private DTODataSource buildDataSource(Audience audience, List<DTODataSourceAttribute> allAttributes) {
        return new DTODataSource(null,
                this.getConnectionData().getId(),
                audience.getName(),
                null,
                audience.getId(),
                null,
                null,
                null,
//                    todo may be dynamic for other sources
                DataSourceType.CUSTOMER,
                null,
                null,
                allAttributes,
                buildTableName(DataSourceType.CUSTOMER, audience.getId()),
                null,
                null
        );
    }

    private List<DTODataSource> buildMultipleDataSources(List<Audience> audiences) {

        List<CompletableFuture<DTODataSource>> allDataSources = audiences.stream()
                .map(audience -> restService.executeAsync(getBaseUrl(),
                new MailChimpMergeFieldsRequest(getApiKey(), audience.getId()), JsonDataConverter.getInstance(),
                mergeFields -> buildDataSource(audience, getDataSourceAttributes(mergeFields))))
                .collect(Collectors.toList());

        return ConcurrentUtils.wait(allDataSources);
    }

    //  todo make dynamic if needed
    private String buildTableName(DataSourceType dataSourceType, String id) {
        return dataSourceType.toString().toLowerCase() + "_" + id;
    }

}
