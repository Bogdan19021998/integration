package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.IHttpSourceDefinition;
import ai.distil.integration.job.sync.holder.MailChimpDataSourceDefinition;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.integration.job.sync.jdbc.TableDefinition;
import ai.distil.integration.utils.MapUtils;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.DataSourceType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.asynchttpclient.Param;
import org.asynchttpclient.Request;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.stream.Collectors;

public class MailChimpHttpConnection extends AbstractHttpConnection {

    private static final String DEFAULT_COUNT_KEY = "count";
    private static final String DEFAULT_OFFSET_KEY = "offset";

    private static final String LISTS_URL = "/lists";
    private static final String MERGE_FIELDS_URL = "/lists/%s/merge-fields";

    private static final TypeReference<AudiencesWrapper> AUDIENCE_TYPE_REFERENCE = new TypeReference<AudiencesWrapper>() {};
    private static final TypeReference<MembersWrapper> MEMBERS_TYPE_REFERENCE = new TypeReference<MembersWrapper>() {};

    public MailChimpHttpConnection(DTOConnection dtoConnection) {
        super(dtoConnection);
    }

    @Override
    public boolean isAvailable() {
        Request request = getBaseGetRequest(LISTS_URL, buildDefaultPageParams(PageRequest.of(0, 1)));
        AudiencesWrapper result = execute(request, AUDIENCE_TYPE_REFERENCE);
        return result != null;
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        Request listRequest = getBaseGetRequest(LISTS_URL);

        AudiencesWrapper result = execute(listRequest, AUDIENCE_TYPE_REFERENCE);

        return result.getList().stream().map(audience -> new DTODataSource(null,
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
        )).collect(Collectors.toList());

    }



    @Override
    public DTODataSource getDataSource(TableDefinition tableDefinition) {
//        todo implement
        return null;
    }


    @Override
    public List<DatasetRow> getNextPage(DTODataSource dtoDataSource, PageRequest pageRequest) {
        IHttpSourceDefinition sourceDefinition = MailChimpDataSourceDefinition.findSourceDefinition(dtoDataSource);
        Request getRequest = getBaseGetRequest(sourceDefinition.urlPart(dtoDataSource), buildDefaultPageParams(pageRequest));
        MembersWrapper response = execute(getRequest, MEMBERS_TYPE_REFERENCE);

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

    private List<Param> buildDefaultPageParams(PageRequest pageRequest) {
        return Lists.newArrayList(
                new Param(DEFAULT_COUNT_KEY, String.valueOf(pageRequest.getPageSize())),
                new Param(DEFAULT_OFFSET_KEY, String.valueOf(pageRequest.getPageNumber() * pageRequest.getPageSize()))
        );
    }

    private List<DTODataSourceAttribute> getDataSourceAttributes(String listId) {
        String url = String.format(MERGE_FIELDS_URL, listId);
        Request mergeFieldsRequest = getBaseGetRequest(url);

        Map<String, Object> mergeFieldsDefinition = execute(mergeFieldsRequest, MAP_TYPE_REFERENCE);

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

}
