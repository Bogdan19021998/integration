package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.integration.job.sync.http.request.IHttpRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.*;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.MapUtils;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.DataSourceType;
import org.springframework.data.domain.PageRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MailChimpHttpConnection extends AbstractHttpConnection {

    public MailChimpHttpConnection(DTOConnection dtoConnection, RestService restService) {
        super(dtoConnection, restService);
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
        return result.getList().stream().map(this::buildDataSource).collect(Collectors.toList());
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition tableDefinition) {
        SingleMailChimpAudienceRequest request = new SingleMailChimpAudienceRequest(getApiKey(), tableDefinition.getDataSourceId());
        return Optional.ofNullable(executeRequest(request))
                .map(this::buildDataSource)
                .orElse(null);
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        SingleMailChimpAudienceRequest request = new SingleMailChimpAudienceRequest(getApiKey(), dataSource.getDataSourceId());
        Audience result = executeRequest(request);
        return result != null;
    }

    @Override
    public List<DatasetRow> getNextPage(DataSourceDataHolder dataSourceHolder, PageRequest pageRequest) {
        MailChimpMembersRequest request = new MailChimpMembersRequest(dataSourceHolder.getDataSourceId(), getApiKey(), pageRequest);
        MembersWrapper response = executeRequest(request);

        return response.getMembers().stream().map(MapUtils::flatten).map(row -> {

            DatasetRow.DatasetRowBuilder builder = new DatasetRow.DatasetRowBuilder();
            row.forEach(builder::addValue);
            return builder.build();
        }).collect(Collectors.toList());
    }

    @Override
    protected String getBaseUrl() {
        return HttpConnectionConfiguration.MAIL_CHIMP.getBaseUrl();
    }

    private List<DTODataSourceAttribute> getDataSourceAttributes(String listId) {
        MailChimpMergeFieldsRequest mergeFieldsRequest = new MailChimpMergeFieldsRequest(getApiKey(), listId);

        Map<String, Object> mergeFieldsDefinition = executeRequest(mergeFieldsRequest);

        return MailChimpMembersFieldsHolder.getAllFields(mergeFieldsDefinition)
                .stream()
                .map(field -> new DTODataSourceAttribute(null,
                        field.getSourceFieldName(),
                        field.getDisplayName(),
                        generateColumnName(field.getSourceFieldName()),
                        field.getAttributeType(),
                        true,
                        field.getAttributeTag(),
                        null,
                        new Date(),
                        new Date())).collect(Collectors.toList());
    }

    private DTODataSource buildDataSource(Audience audience) {
        return new DTODataSource(null,
                this.getConnectionData().getId(),
                buildTableName(DataSourceType.CUSTOMER, audience.getId()),
                null,
                audience.getId(),
                null,
                null,
                null,
//                    todo may be dynamic for other sources
                DataSourceType.CUSTOMER,
                null,
                null,
                getDataSourceAttributes(audience.getId()),
                null
        );
    }

    private <T> T executeRequest(IHttpRequest<T> r) {
        return this.restService.execute(getBaseUrl(), r, JsonDataConverter.getInstance());
    }

    private String getApiKey() {
        return Optional.ofNullable(this.getConnectionData())
                .map(DTOConnection::getConnectionSettings)
                .map(ConnectionSettings::getApiKey)
                .orElse(null);
    }

//  todo make dynamic if needed
    private String buildTableName(DataSourceType dataSourceType, String id) {
        return dataSourceType.toString().toLowerCase() + "_" + id;
    }

}
