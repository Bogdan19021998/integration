package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.vo.*;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpAudiencesRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMergeFieldsRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.CreateListMailChimpRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.CreateMergeFieldMailChimpRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.UpsertMemberMailChimpRequest;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.HashHelper;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

public class MailChimpDataSync extends AbstractDataSync<MailChimpCustomFieldsHttpConnection, InsertMember> {

    public static final String TEXT_TYPE = "text";
    public static final String DEFAULT_MEMBER_STATUS = "subscribed";
    public static final String EMAIL_ID_FIELD = "email_address";
    public static final int MAX_TAG_LENGTH = 10;

    public MailChimpDataSync(DestinationIntegrationDTO destinationIntegration, List<DTODataSourceAttributeExtended> attributes, SyncSettings syncSettings,
                             DTOConnection connection, RestService restService) {
        super(destinationIntegration, attributes, syncSettings, null);

        this.httpConnection = new MailChimpCustomFieldsHttpConnection(connection, restService, null,
                Lists.newArrayList(EMAIL_ID_FIELD));

    }


    @Override
    public String createListIfNotExists() {

        AudiencesWrapper result = this.httpConnection.executeRequest(new MailChimpAudiencesRequest(this.httpConnection.getApiKey()));
        Map<String, String> existingLists = ListUtils.groupByWithOverwrite(result.getList(), Audience::getName, Audience::getId);
        String listName = buildListName(this.destinationIntegration.getId());

        return Optional.ofNullable(existingLists.get(listName))
                .orElseGet(() -> Optional.ofNullable(this.httpConnection.executeRequest(new CreateListMailChimpRequest(this.httpConnection.getApiKey(), new MailChimpList(listName,
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
                )))).map(Audience::getId).orElseThrow(() -> new RuntimeException(String.format("Can't create audience by name - {}", listName))));

    }

    private Contact buildContactInfo() {
        return new Contact("Distil", "address1", "address2", "city",
                "state", "zip", "country", "123123123");
    }

    @Override
    public List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId) {

        Map<String, Object> response = this.httpConnection.executeRequest(new MailChimpMergeFieldsRequest(this.httpConnection.getApiKey(), listId));


        Map<String, String> existingFields = Optional.ofNullable(response.get(MailChimpMembersFieldsHolder.MERGE_FIELDS_KEY)).map((mergeFields) -> {
            List<Map<String, Object>> mergeFieldsMap = (List<Map<String, Object>>) mergeFields;

            return ListUtils.groupByWithOverwrite(mergeFieldsMap,
                    m -> (String) m.get(MailChimpMembersFieldsHolder.NAME_KEY),
                    m -> String.valueOf(m.get(MailChimpMembersFieldsHolder.TAG_KEY)));
        }).orElse(ImmutableMap.of());


        return this.attributes.stream().map(attr -> {
            String fieldName = buildCustomFieldName(attr.getAttributeDistilName(), attr.getId());
            String tag = buildTag(attr.getAttributeDistilName(), attr.getId());

            return Optional.ofNullable(existingFields.get(fieldName))
                    .map(fieldId -> new CustomAttributeDefinition(fieldId, fieldName,
                            attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition()))
                    .orElseGet(() -> {
                        MailChimpMergeField field = new MailChimpMergeField(tag, fieldName, TEXT_TYPE, false,
                                null, true, null, null, null);

                        return Optional.ofNullable(this.httpConnection.executeRequest(new CreateMergeFieldMailChimpRequest(this.httpConnection.getApiKey(), listId, field)))
                                .map(r -> new CustomAttributeDefinition(r.getTag(), fieldName,
                                        attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition()))
                                .orElse(null);
                    });
        }).collect(Collectors.toList());
    }

    private String buildTag(String name, Long id) {
        String result = this.buildCustomFieldName(name, id);
        return result.substring(Math.max(0, result.length() - MAX_TAG_LENGTH)).toUpperCase();
    }

    //    todo batch operations
//    https://mailchimp.com/developer/guides/how-to-use-batch-operations/#Use_Batch_Operations
//    public void ingestData(String listId, List<CustomAttributeDefinition> attributes, List<CustomerRecord> data);

    @Override
    public Set<String> retrieveCurrentEmails(String listId) {
        Set<String> existingEmails = new HashSet<>(10000);
        IRowIterator iterator = this.httpConnection.getIterator(new DataSourceDataHolder(listId, null, Lists.newArrayList(), DataSourceType.CUSTOMER, null));
        iterator.forEachRemaining(row -> existingEmails.add(String.valueOf(row.getValues().stream().findFirst().map(DatasetValue::getValue))));

        return existingEmails;
    }

    @Override
    protected InsertMember getSubscriber() {
        InsertMember insertMember = new InsertMember();
        insertMember.setStatus(MailChimpDataSync.DEFAULT_MEMBER_STATUS);
        insertMember.setMergeFields(new HashMap<>(50));
        return insertMember;
    }

    @Override
    protected void setEmail(InsertMember subscriber, String value) {
        subscriber.setEmailAddress(value);
    }

    @Override
    protected String getEmailAddress(InsertMember subscriber) {
        return subscriber.getEmailAddress();
    }

    @Override
    protected void addCustomField(InsertMember subscriber, String fieldName, String value) {
        subscriber.getMergeFields().put(fieldName, value);
    }

    @Override
    protected void sendSubscribers(String listId, List<InsertMember> subscribers) {
        subscribers.forEach(subscriber -> {
            String hash = HashHelper.md5Hash(subscriber.getEmailAddress());
            this.httpConnection.executeRequest(new UpsertMemberMailChimpRequest(this.httpConnection.getApiKey(), listId, hash, subscriber));
        });
    }

    @Override
    protected void removeSubscribers(List<InsertMember> subscribers) {
        //  todo implement
    }

}
