package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMembersWithSpecificFieldsRequest;
import ai.distil.integration.service.RestService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MailChimpCustomFieldsHttpConnection extends MailChimpHttpConnection {

    public static final String MEMBERS_KEY = "members";
    private List<String> fields;

    public MailChimpCustomFieldsHttpConnection(DTOConnection dtoConnection, RestService restService, MailChimpMembersFieldsHolder fieldsHolder, List<String> fields) {
        super(dtoConnection, restService, fieldsHolder);
        this.fields = fields;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSourceHolder, DatasetPageRequest pageRequest) {
        MailChimpMembersWithSpecificFieldsRequest request = new MailChimpMembersWithSpecificFieldsRequest(
                dataSourceHolder.getDataSourceId(),
                getApiKey(),
                pageRequest,
                fields.stream().map(field -> MEMBERS_KEY + "." + field).collect(Collectors.toList()));

        MembersWrapper response = executeRequest(request);

        return new DatasetPage(Optional.ofNullable(response).map(MembersWrapper::getMembers).orElse(Collections.emptyList())
                .stream()
                .map(row -> new DatasetRow(fields.stream().map(field -> new DatasetValue(row.get(field), field)).collect(Collectors.toList())))
                .collect(Collectors.toList()), null);
    }

}
