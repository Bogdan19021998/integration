package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.destination.vo.SendSubscribersResult;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.JsonTarDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.*;
import ai.distil.integration.job.sync.http.mailchimp.vo.batch.BatchOperationResult;
import ai.distil.integration.job.sync.http.mailchimp.vo.batch.BatchTrackingResponse;
import ai.distil.integration.job.sync.http.request.mailchimp.*;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.*;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.HashHelper;
import ai.distil.integration.utils.ListUtils;
import ai.distil.integration.utils.WaitUtils;
import ai.distil.model.org.destination.IntegrationSettings;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MailChimpDataSync extends AbstractDataSync<MailChimpCustomFieldsHttpConnection, InsertMember> {

    private static final String TEXT_TYPE = "text";
    private static final String DEFAULT_MEMBER_STATUS = "subscribed";
    private static final String EMAIL_ID_FIELD = "email_address";
    private static final String FINISHED_BATCH_STATE = "finished";

    public MailChimpDataSync(DestinationDTO destination, DestinationIntegrationDTO destinationIntegration, List<DTODataSourceAttributeExtended> attributes, SyncSettings syncSettings,
                             DTOConnection connection, RestService restService) {
        super(destination, destinationIntegration, attributes, syncSettings, null);

        this.httpConnection = new MailChimpCustomFieldsHttpConnection(connection, restService, null,
                Lists.newArrayList(EMAIL_ID_FIELD), Lists.newArrayList(String.valueOf(buildFieldId(DEFAULT_HASH_CODE_FIELD_ID))));

    }


    @Override
    public IntegrationSettings findIntegrationSettings() {
        RetrieveAccountInfoMailChimpRequest request = new RetrieveAccountInfoMailChimpRequest(this.httpConnection.getApiKey());

        return Optional.ofNullable(this.httpConnection.executeRequest(request))
                .map(accountInfo -> {
                    Integer fieldsCount = accountInfo.getProEnabled() ? 80 : 30;
                    return new IntegrationSettings(fieldsCount, null, null);
                }).orElse(null);
    }

    @Override
    public String createListIfNotExists() {
        AudiencesWrapper result = this.httpConnection.executeRequest(new MailChimpAudiencesRequest(this.httpConnection.getApiKey()));
        Map<String, String> existingLists = ListUtils.groupByWithOverwrite(result.getList(), Audience::getId, Audience::getName);

        String listName = buildListName(this.destination.getTitle());
        String listId = StringUtils.isEmpty(this.destinationIntegration.getListId()) ? null : this.destinationIntegration.getListId();

        return Optional.ofNullable(listId).map(list -> Optional.ofNullable(existingLists.get(list)).map(existingListName -> {
            if (!existingListName.equalsIgnoreCase(listName)) {
                MailChimpList mailChimpList = buildDefaultMailChimpList(listName);
                EditListMailChimpRequest request = new EditListMailChimpRequest(this.httpConnection.getApiKey(), mailChimpList, listId);
                this.httpConnection.executeRequest(request);
            }
            return list;
        }).orElseThrow(() -> new RuntimeException(String.format("Looks like mail chimp audience has been removed, id - %s", list)))).orElseGet(() -> {
            MailChimpList list = buildDefaultMailChimpList(listName);
            return Optional.ofNullable(this.httpConnection.executeRequest(new CreateListMailChimpRequest(this.httpConnection.getApiKey(), list)))
                    .map(Audience::getId)
                    .orElseThrow(() -> new RuntimeException(String.format("Can't create audience by name - {}", listName)));
        });
    }

    @Override
    public List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId) {
        Map<String, Object> response = this.httpConnection.executeRequest(new MailChimpMergeFieldsRequest(this.httpConnection.getApiKey(), listId));


        Map<String, String> existingFields = Optional.ofNullable(response.get(MailChimpMembersFieldsHolder.MERGE_FIELDS_KEY)).map((mergeFields) -> {
            List<Map<String, Object>> mergeFieldsMap = (List<Map<String, Object>>) mergeFields;

            return ListUtils.groupByWithOverwrite(mergeFieldsMap,
                    m -> (String) m.get(MailChimpMembersFieldsHolder.TAG_KEY),
                    m -> String.valueOf(m.get(MailChimpMembersFieldsHolder.NAME_KEY)));
        }).orElse(ImmutableMap.of());


        return this.retrieveAllAttributesBySettings().stream().map(attr -> {
            String fieldName = attr.getAttributeDisplayName();
            String fieldId = buildFieldId(attr.getId());

            return Optional.ofNullable(existingFields.get(fieldId))
                    .map(existingFieldName -> new CustomAttributeDefinition(fieldId, fieldName,
                            attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition(), attr.getAttributeType()))
                    .orElseGet(() -> {
                        MailChimpMergeField field = new MailChimpMergeField(fieldId, fieldName, TEXT_TYPE, false,
                                null, true, null, null, null);

                        return Optional.ofNullable(this.httpConnection.executeRequest(new CreateMergeFieldMailChimpRequest(this.httpConnection.getApiKey(), listId, field)))
                                .map(r -> new CustomAttributeDefinition(r.getTag(), fieldName,
                                        attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition(), attr.getAttributeType()))
                                .orElse(null);
                    });
        }).collect(Collectors.toList());
    }


//    todo batch operations
//    https://mailchimp.com/developer/guides/how-to-use-batch-operations/#Use_Batch_Operations
//    public void ingestData(String listId, List<CustomAttributeDefinition> attributes, List<CustomerRecord> data);

    @Override
    public Map<String, String> retrieveCurrentUsersAndHashes(String listId) {
        Map<String, String> existingEmails = new HashMap<>(10000);
        IRowIterator iterator = this.httpConnection.getIterator(new DataSourceDataHolder(listId, null, Lists.newArrayList(), DataSourceType.CUSTOMER, null));

        iterator.forEachRemaining(row -> {

            String email = null;
            String hash = null;

            for (DatasetValue value : row.getValues()) {
                if (HASH_CODE_FIELD_NAME.equalsIgnoreCase(value.getAlias())) {
                    hash = (String) value.getValue();
                } else {
                    email = (String) value.getValue();
                }
            }

            if (email != null && hash != null) {
                existingEmails.put(email, hash);
            } else {
                log.warn("Empty hash or email value for the subscriber -> row data {}", row);
            }

        });

        return existingEmails;
    }

    @Override
    protected InsertMember buildBaseSubscriber() {
        InsertMember insertMember = new InsertMember();
        insertMember.setStatusIfNew(MailChimpDataSync.DEFAULT_MEMBER_STATUS);
        insertMember.setMergeFields(new HashMap<>(50));
        return insertMember;
    }

    @Override
    protected void addCustomField(InsertMember subscriber, String fieldName, String value) {
        subscriber.getMergeFields().put(fieldName, value);
    }

    @Override
    protected SendSubscribersResult sendSubscribers(String listId, List<InsertMember> subscribers) {

        Set<String> failedSubscribers = new HashSet<>();
        long successCount = 0;

        List<UpsertMemberMailChimpRequest> requests = subscribers.stream().map(subscriber -> {
            String email = subscriber.getEmailAddress().toLowerCase();
            String hash = HashHelper.md5Hash(email).toLowerCase();
            return new UpsertMemberMailChimpRequest(this.httpConnection.getApiKey(), listId, hash, subscriber);
        }).collect(Collectors.toList());

        BatchRequest batchRequest = new BatchRequest(this.httpConnection.getApiKey(), requests, JsonDataConverter.getInstance());
        BatchTrackingResponse batchResponse = this.httpConnection.executeRequest(batchRequest);

        BatchTrackingResponse result = WaitUtils.wait(() -> this.httpConnection.executeRequest(new GetBatchDataRequest(this.httpConnection.getApiKey(), batchResponse.getId())),
                r -> FINISHED_BATCH_STATE.equalsIgnoreCase(r.getStatus()), 1000);

        List<BatchOperationResult<Member>> batchResult = this.httpConnection.executeRequest(result.getResponseBodyUrl(), new GetBatchResultRequest<>(), JsonTarDataConverter.getInstance());

        return buildSendSubscribersResultFromBatchResponse(batchResult);
    }

    private SendSubscribersResult buildSendSubscribersResultFromBatchResponse(List<BatchOperationResult<Member>> results) {

        long success = 0;
        Set<String> failedEmails = new HashSet<>();

        for (BatchOperationResult result : results) {
            if(result.getStatusCode().equals(HttpStatus.OK.value())) {
                success++;
            } else {
//                failedEmails.add();
                System.out.println();
            }
        }
        return new SendSubscribersResult(failedEmails, success);

    }

    @Override
    protected void removeSubscribers(String listId, Collection<String> subscribersIds) {
        subscribersIds.forEach(subscribersId -> {
            String hash = HashHelper.md5Hash(subscribersId.toLowerCase()).toLowerCase();
            DeleteMemberMailChimpRequest deleteRequest = new DeleteMemberMailChimpRequest(this.httpConnection.getApiKey(), listId, hash);
            this.httpConnection.executeRequest(deleteRequest);
        });
    }

    private MailChimpList buildDefaultMailChimpList(String listName) {
        return new MailChimpList(listName,
                buildContactInfo(),
                "Permission Reminder",
                false,
                new CampaignDefaults("Distil", "test@distil.ai", "subject", "EN"),
                null,
                null,
                true,
                "pub",
                true,
                false
        );
    }

    private Contact buildContactInfo() {
//        todo add real info?
        return new Contact("Distil", "address1", "address2", "city",
                "state", "zip", "country", "123123123");
    }

}
