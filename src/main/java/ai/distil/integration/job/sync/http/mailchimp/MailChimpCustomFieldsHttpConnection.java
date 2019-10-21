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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MailChimpCustomFieldsHttpConnection extends MailChimpHttpConnection {

    public static final String MEMBERS_KEY = "members";
    public static final String MERGE_FIELDS_KEY = "merge_fields";
    private List<String> fields;
    private List<String> mergeFields;

    public MailChimpCustomFieldsHttpConnection(DTOConnection dtoConnection, RestService restService, MailChimpMembersFieldsHolder fieldsHolder, List<String> fields, List<String> mergeFields) {
        super(dtoConnection, restService, fieldsHolder);
        this.fields = fields;
        this.mergeFields = mergeFields;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSourceHolder, DatasetPageRequest pageRequest) {
        List<String> fieldsToProcess = Stream.concat(
                fields.stream().map(field -> MEMBERS_KEY + "." + field),
                mergeFields.stream().map(field -> MEMBERS_KEY + "." + MERGE_FIELDS_KEY + "." + field)
        ).collect(Collectors.toList());

        MailChimpMembersWithSpecificFieldsRequest request = new MailChimpMembersWithSpecificFieldsRequest(
                dataSourceHolder.getDataSourceId(),
                getApiKey(),
                pageRequest,
                fieldsToProcess);

        MembersWrapper response = executeRequest(request);

        return new DatasetPage(Optional.ofNullable(response).map(MembersWrapper::getMembers).orElse(Collections.emptyList())
                .stream()
                .map(row -> {

                    List<DatasetValue> fields = Stream.concat(
                            this.fields.stream().map(field -> new DatasetValue(row.get(field), field)),
                            mergeFields.stream().map(field -> new DatasetValue(((Map<String, Object>) row.get(MERGE_FIELDS_KEY)).get(field), field)))
                            .collect(Collectors.toList());
                    return new DatasetRow(fields);
                })
                .collect(Collectors.toList()), null);
    }

}
