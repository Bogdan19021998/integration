package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.job.destination.IDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.*;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpAudiencesRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMergeFieldsRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.CreateListMailChimpRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.CreateMergeFieldMailChimpRequest;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.ConcurrentUtils;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.org.destination.DestinationIntegration;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MailChimpDataSync extends MailChimpHttpConnection implements IDataSync {

    public static final String FAKE_NAME_FOR_ATTR = "FAKE_NAME";
    public static final String TEXT_TYPE = "text";
    private DestinationIntegration destinationIntegration;

    public MailChimpDataSync(DTOConnection dtoConnection, DestinationIntegration destinationIntegration, RestService restService, MailChimpMembersFieldsHolder fieldsHolder) {
        super(dtoConnection, restService, fieldsHolder);
        this.destinationIntegration = destinationIntegration;
    }

    @Override
    public String createListIfNotExists() {

        AudiencesWrapper result = executeRequest(new MailChimpAudiencesRequest(getApiKey()));
        Map<String, String> existingLists = ListUtils.groupByWithOverwrite(result.getList(), Audience::getName, Audience::getId);
        String listName = buildListName(this.destinationIntegration.getFkDestinationId());

        return Optional.ofNullable(existingLists.get(listName))
                .orElseGet(() -> Optional.ofNullable(executeRequest(new CreateListMailChimpRequest(getApiKey(), new MailChimpList(listName,
                        buildContactInfo(),
                        "Permission Reminder",
                        false,
                        new CampaignDefaults("Distil", "test@alskdjfsd.com", "subject", "EN"),
                        null,
                        null,
                        true,
                        "pub",
                        true,
                        false
                )))).map(Audience::getId).orElseThrow(() -> new RuntimeException(String.format("Can't create audience by name - {}", listName))));

    }

    private Contact buildContactInfo() {
        return new Contact("Distil", "address1", "address2", "city",
                "state", "zip", "country", "123123123");
    }

    @Override
    public List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId) {

        Map<String, Object> response = executeRequest(new MailChimpMergeFieldsRequest(getApiKey(), listId));


        Map<String, String> existingFields = Optional.ofNullable(response.get(MailChimpMembersFieldsHolder.MERGE_FIELDS_KEY)).map((mergeFields) -> {
            List<Map<String, Object>> mergeFieldsMap = (List<Map<String, Object>>) mergeFields;

            return ListUtils.groupByWithOverwrite(mergeFieldsMap,
                    m -> (String) m.get(MailChimpMembersFieldsHolder.NAME_KEY),
                    m -> String.valueOf(m.get(MailChimpMembersFieldsHolder.MERGE_ID_KEY)));
        }).orElse(ImmutableMap.of());


        return ConcurrentUtils.wait(this.destinationIntegration.getAttributes().stream().map(attr -> {
            String fieldName = buildCustomFieldName(FAKE_NAME_FOR_ATTR, attr.getId());
            return Optional.ofNullable(existingFields.get(fieldName))
                    .map(fieldId -> CompletableFuture.completedFuture(new CustomAttributeDefinition(fieldId, fieldName, attr.getAttributeDataTag(), attr.getFkDataSourceAttributeId())))
                    .orElseGet(() -> {
//                        todo add tag generation (10 characters)
                        MailChimpMergeField field = new MailChimpMergeField(null, fieldName, TEXT_TYPE, false,
                                null, true, null, null, null);

                        return this.executeAsyncRequest(new CreateMergeFieldMailChimpRequest(getApiKey(), listId, field))
                                .thenApply(r -> new CustomAttributeDefinition(String.valueOf(r.getMergeId()), fieldName,
                                        attr.getAttributeDataTag(), attr.getFkDataSourceAttributeId()));

                    });
        }).collect(Collectors.toList()));
    }

    @Override
    public void ingestData(String listId, List<CustomAttributeDefinition> attributes) {

    }
}
