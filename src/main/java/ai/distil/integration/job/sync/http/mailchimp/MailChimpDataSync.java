package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.destination.IDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.*;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpAudiencesRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMembersWithSpecificFieldsRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMergeFieldsRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.CreateListMailChimpRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.CreateMergeFieldMailChimpRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.UpsertMemberMailChimpRequest;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.ConcurrentUtils;
import ai.distil.integration.utils.HashHelper;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static ai.distil.model.types.DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS;

public class MailChimpDataSync extends MailChimpHttpConnection implements IDataSync {

    public static final String FAKE_NAME_FOR_ATTR = "FAKE_NAME";
    public static final String TEXT_TYPE = "text";
    public static final String DEFAULT_MEMBER_STATUS = "subscribed";
    public static final String EMAIL_ID_FIELD = "email_address";
    public static final String MEMBERS_KEY = "members";

    private DestinationIntegrationDTO destinationIntegration;

    public MailChimpDataSync(DTOConnection dtoConnection, DestinationIntegrationDTO destinationIntegration, RestService restService, MailChimpMembersFieldsHolder fieldsHolder) {
        super(dtoConnection, restService, fieldsHolder);
        this.destinationIntegration = destinationIntegration;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSourceHolder, DatasetPageRequest pageRequest) {
        MailChimpMembersWithSpecificFieldsRequest request = new MailChimpMembersWithSpecificFieldsRequest(
                dataSourceHolder.getDataSourceId(),
                getApiKey(),
                pageRequest,
                Lists.newArrayList(MEMBERS_KEY + "." + EMAIL_ID_FIELD));

        MembersWrapper response = executeRequest(request);

        return new DatasetPage(Optional.ofNullable(response).map(MembersWrapper::getMembers).orElse(Collections.emptyList())
                .stream()
                .map(row -> new DatasetRow(Lists.newArrayList(new DatasetValue(row.get(EMAIL_ID_FIELD), EMAIL_ID_FIELD))))
                .collect(Collectors.toList()), null);
    }



    @Override
    public String createListIfNotExists() {

        AudiencesWrapper result = executeRequest(new MailChimpAudiencesRequest(getApiKey()));
        Map<String, String> existingLists = ListUtils.groupByWithOverwrite(result.getList(), Audience::getName, Audience::getId);
        String listName = buildListName(this.destinationIntegration.getId());

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
                    m -> String.valueOf(m.get(MailChimpMembersFieldsHolder.TAG_KEY)));
        }).orElse(ImmutableMap.of());


        return ConcurrentUtils.wait(this.destinationIntegration.getAttributes().stream().map(attr -> {
            String fieldName = buildCustomFieldName(FAKE_NAME_FOR_ATTR, attr.getAttributeId());
            return Optional.ofNullable(existingFields.get(fieldName))
                    .map(fieldId -> CompletableFuture.completedFuture(new CustomAttributeDefinition(fieldId, fieldName, attr.getAttributeDataTag(), attr.getAttributeId())))
                    .orElseGet(() -> {
//                        todo add tag generation (10 characters)
                        MailChimpMergeField field = new MailChimpMergeField(null, fieldName, TEXT_TYPE, false,
                                null, true, null, null, null);

                        return this.executeAsyncRequest(new CreateMergeFieldMailChimpRequest(getApiKey(), listId, field))
                                .thenApply(r -> new CustomAttributeDefinition(String.valueOf(r.getTag()), fieldName,
                                        attr.getAttributeDataTag(), attr.getAttributeId()));

                    });
        }).collect(Collectors.toList()));
    }

    //    todo batch operations
//    https://mailchimp.com/developer/guides/how-to-use-batch-operations/#Use_Batch_Operations
    @Override
    public void ingestData(String listId, List<CustomAttributeDefinition> attributes) {
        List<String> currentEmails = retrieveCurrentEmails(listId);

        for (int i = 0; i < 10; i++) {
            InsertMember insertMember = generateMockData(attributes);
            String hash = HashHelper.md5Hash(insertMember.getEmailAddress());
            Member result = executeRequest(new UpsertMemberMailChimpRequest(getApiKey(), listId, hash, insertMember));
            System.out.println();
        }

    }

    @Override
    public List<String> retrieveCurrentEmails(String listId) {
        List<String> existingEmails = new ArrayList<>(10000);
        IRowIterator iterator = getIterator(new DataSourceDataHolder(listId, null, Lists.newArrayList(), DataSourceType.CUSTOMER, null));
        iterator.forEachRemaining(row -> existingEmails.add(String.valueOf(row.getValues().stream().findFirst().map(DatasetValue::getValue))));

        return existingEmails;
    }

    private InsertMember generateMockData(List<CustomAttributeDefinition> attributes) {
        InsertMember result = new InsertMember();
        result.setMergeFields(new HashMap<>());
        result.setStatusIfNew(DEFAULT_MEMBER_STATUS);

        attributes.forEach(attr -> {
            if (CUSTOMER_EMAIL_ADDRESS.equals(attr.getTag())) {
                result.setEmailAddress(new Random().nextInt() + "@fake123.com");
            } else {
                result.getMergeFields().put(attr.getId(), String.valueOf(new Random().nextLong()));
            }
        });

        return result;
    }
}
