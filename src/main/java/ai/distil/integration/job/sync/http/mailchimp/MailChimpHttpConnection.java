package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.integration.job.sync.http.request.mailchimp.*;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.utils.MapUtils;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.DataSourceType;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.stream.Collectors;

public class MailChimpHttpConnection extends AbstractHttpConnection {

    public MailChimpHttpConnection(DTOConnection dtoConnection) {
        super(dtoConnection);
    }

    @Override
    public boolean isAvailable() {
        AnyMailChimpAudienceRequest request = new AnyMailChimpAudienceRequest();
        AudiencesWrapper result = execute(request);
        return result != null;
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        AudiencesWrapper result = execute(new MailChimpAudiencesRequest());
        return result.getList().stream().map(this::buildDataSource).collect(Collectors.toList());
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition tableDefinition) {
        SingleMailChimpAudienceRequest request = new SingleMailChimpAudienceRequest(tableDefinition.getDataSourceId());
        return Optional.ofNullable(execute(request))
                .map(this::buildDataSource)
                .orElse(null);
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        SingleMailChimpAudienceRequest request = new SingleMailChimpAudienceRequest(dataSource.getDataSourceId());
        Audience result = execute(request);
        return result != null;
    }

    @Override
    public List<DatasetRow> getNextPage(DataSourceDataHolder dataSourceHolder, PageRequest pageRequest) {
        MailChimpMembersRequest request = new MailChimpMembersRequest(dataSourceHolder.getDataSourceId(), pageRequest);
        MembersWrapper response = execute(request);

        return response.getMembers().stream().map(MapUtils::flatten).map(row -> {

            DatasetRow.DatasetRowBuilder builder = new DatasetRow.DatasetRowBuilder();
            row.forEach(builder::addValue);
            return builder.build();
        }).collect(Collectors.toList());
    }

    @Override
    protected Map<String, Object> getDefaultHeaders() {
        Map<String, Object> headers = new HashMap<>(super.getDefaultHeaders());

        String apiKey = Optional.ofNullable(this.getConnectionData())
                .map(DTOConnection::getConnectionSettings)
                .map(ConnectionSettings::getApiKey)
                .orElse(null);

        headers.put(AUTH_HEADER_KEY, String.format("Basic %s", apiKey));

        return headers;
    }

    @Override
    protected IDataConverter getDataConverter() {
        return JsonDataConverter.getInstance();
    }

    @Override
    protected String getBaseUrl() {
        return HttpConnectionConfiguration.MAIL_CHIMP.getBaseUrl();
    }

    private List<DTODataSourceAttribute> getDataSourceAttributes(String listId) {
        MailChimpMergeFieldsRequest mergeFieldsRequest = new MailChimpMergeFieldsRequest(listId);

        Map<String, Object> mergeFieldsDefinition = execute(mergeFieldsRequest);

        return MailChimpMembersFieldsHolder.getAllFields(mergeFieldsDefinition)
                .stream()
                .map(field -> new DTODataSourceAttribute(null,
                        field.getSourceFieldName(),
                        field.getDisplayName(),
                        generateColumnName(field.getSourceFieldName()),
                        field.getAttributeType(),
                        true,
//                            todo add tagging
                        null,
                        null,
                        new Date(),
                        new Date())).collect(Collectors.toList());
    }

    private DTODataSource buildDataSource(Audience audience) {
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
                getDataSourceAttributes(audience.getId()),
                null
        );
    }

}
