package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetColumnType;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.sync.holder.IHttpSourceDefinition;
import ai.distil.integration.job.sync.holder.MailChimpDataSourceDefinitions;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.model.org.ConnectionSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.asynchttpclient.Param;
import org.asynchttpclient.Request;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MailChimpHttpConnection extends AbstractHttpConnection {

    private static final String DEFAULT_COUNT_KEY = "count";
    private static final String DEFAULT_OFFSET_KEY = "offset";

    private static final String LISTS_URL = "/lists";


    private static final TypeReference<AudiencesWrapper> AUDIENCE_TYPE_REFERENCE = new TypeReference<AudiencesWrapper>() {};
    private static final TypeReference<MembersWrapper> MEMBERS_TYPE_REFERENCE = new TypeReference<MembersWrapper>() {};

    public MailChimpHttpConnection(DTOConnection dtoConnection) {
        super(dtoConnection);
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        Request request = getBaseGetRequest(LISTS_URL);
        AudiencesWrapper result = execute(request, AUDIENCE_TYPE_REFERENCE);
        return Lists.newArrayList();
    }

    @Override
    public List<DatasetRow> getNextPage(DTODataSource dtoDataSource, PageRequest pageRequest) {
        IHttpSourceDefinition sourceDefinition = MailChimpDataSourceDefinitions.findSourceDefinition(dtoDataSource);
        Request getRequest = getBaseGetRequest(sourceDefinition.urlPart(dtoDataSource), buildDefaultPageParams(pageRequest));
        MembersWrapper membersWrapper = execute(getRequest, MEMBERS_TYPE_REFERENCE);

        List<DatasetRow> collect = membersWrapper.getMembers().stream().map((v) -> new DatasetRow(Lists.newArrayList(
                new DatasetValue(v.getEmailAddress(), "alias", DatasetColumnType.STRING)
        ))).collect(Collectors.toList());

        return collect;
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

}
