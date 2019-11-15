package ai.distil.integration.job.sync.http.campmon;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.campmon.request.CustomListFieldsCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.DeleteSubscriberCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateCustomFieldCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateListCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.ImportSubscribersCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.UpdateListCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateCustomFieldBody;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.ListBody;
import ai.distil.integration.job.sync.http.campmon.vo.*;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.ConcurrentUtils;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.org.destination.IntegrationSettings;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static ai.distil.integration.job.sync.http.campmon.vo.Subscriber.DISTIL_HASH_FIELD;

@Slf4j
public class CampaignMonitorDataSync extends AbstractDataSync<CampaignMonitorWithCustomFieldsHttpConnection, Subscriber> {

    public static final String DEFAULT_UNSUBSCRIBE_SETTINGS = "AllClientLists";
    public static final String DEFAULT_FIELD_TYPE = "Text";

    private static final String EMAIL_ADDRESS_FIELD = "EmailAddress";

    public CampaignMonitorDataSync(DestinationDTO destination, DestinationIntegrationDTO destinationIntegration, List<DTODataSourceAttributeExtended> attributes, SyncSettings syncSettings,
                                   DTOConnection connection, RestService restService) {
        super(destination, destinationIntegration, attributes, syncSettings, null);
        this.httpConnection = new CampaignMonitorWithCustomFieldsHttpConnection(connection, restService, null,
                Lists.newArrayList(EMAIL_ADDRESS_FIELD),
                Lists.newArrayList(DISTIL_HASH_FIELD));
    }


    @Override
    public IntegrationSettings findIntegrationSettings() {
//      https://help.campaignmonitor.com/subscriber-custom-fields
        return new IntegrationSettings(50, null, null);
    }

    @Override
    public String createListIfNotExists() {
        List<Client> clients = this.httpConnection.requestAllClients().orElse(Collections.emptyList());
        clients.sort(Comparator.comparing(Client::getClientId));

        if (clients.isEmpty()) {
            throw new RuntimeException("There is no client to save data to.");
        }

        Client client = clients.get(0);
        String listName = buildListName(this.destination.getTitle());
        String listIdValue = StringUtils.isEmpty(this.destinationIntegration.getListId()) ? null : this.destinationIntegration.getListId();

        return Optional.ofNullable(listIdValue).map(listId -> {

            Link existingLink = ConcurrentUtils.wait(this.httpConnection.requestLists(client))
                    .flatMap(links -> links.stream().filter(link -> listId.equalsIgnoreCase(link.getListId())).findFirst())
                    .orElseThrow(() -> new RuntimeException(String.format("Campaign monitor list has been removed - %s", listId)));

            if (!listName.equalsIgnoreCase(existingLink.getName())) {
                existingLink.setName(listName);
                ListBody listBody = new ListBody(listName, DEFAULT_UNSUBSCRIBE_SETTINGS, true);
                UpdateListCampaignMonitorRequest updateRequest = new UpdateListCampaignMonitorRequest(listId, this.httpConnection.getApiKey(), listBody);

                this.httpConnection.executeRequest(updateRequest);
            }
            return listId;

        }).orElseGet(() -> {
            ListBody createListBody = new ListBody(listName, DEFAULT_UNSUBSCRIBE_SETTINGS, false);
            CreateListCampaignMonitorRequest request = new CreateListCampaignMonitorRequest(client.getClientId(), this.httpConnection.getConnectionSettings().getApiKey(), createListBody);

            return this.httpConnection.executeRequest(request);
        });
    }

    @Override
    public List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId) {

        List<CustomAttributeDefinition> result = new ArrayList<>();

        List<CustomFieldDefinition> customFieldDefinitions = Optional.ofNullable(this.httpConnection.getRestService().execute(this.httpConnection.getBaseUrl(),
                new CustomListFieldsCampaignMonitorRequest(this.httpConnection.getConnectionSettings().getApiKey(), listId),
                JsonDataConverter.getInstance())).orElse(Collections.emptyList());

//        column key by field name
        Map<String, String> currentCustomFields = ListUtils.groupByWithOverwrite(customFieldDefinitions,
                CustomFieldDefinition::getFieldName,
                CustomFieldDefinition::getKey);

        List<CustomAttributeDefinition> createdAttributes = ConcurrentUtils.wait(retrieveAllAttributesBySettings().stream().filter(attr -> Optional.ofNullable(currentCustomFields.get(attr.getAttributeDisplayName()))
                .map(fieldName -> {
                    result.add(new CustomAttributeDefinition(fieldName, attr.getAttributeDisplayName(), attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition()));
                    return false;
                })
                .orElse(true)).map(attr -> {
            CreateCustomFieldBody body = new CreateCustomFieldBody(attr.getAttributeDisplayName(), DEFAULT_FIELD_TYPE, true);

            CreateCustomFieldCampaignMonitorRequest request = new CreateCustomFieldCampaignMonitorRequest(listId, this.httpConnection.getConnectionSettings().getApiKey(), body);
            return this.httpConnection.getRestService()
                    .executeAsync(this.httpConnection.getBaseUrl(), request, JsonDataConverter.getInstance())
                    .thenApply(key -> new CustomAttributeDefinition(key, attr.getAttributeDisplayName(), attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition()));

        }).collect(Collectors.toList()));

        result.addAll(createdAttributes);

        return result;

    }

    @Override
    protected void sendSubscribers(String listId, List<Subscriber> subscribers) {
        ImportSubscribersCampaignMonitorRequest importRequest = new ImportSubscribersCampaignMonitorRequest(this.httpConnection.getConnectionSettings().getApiKey(),
                listId, new Subscribers(subscribers));

        this.httpConnection.getRestService().execute(this.httpConnection.getBaseUrl(), importRequest, JsonDataConverter.getInstance());
    }

    @Override
    protected void removeSubscribers(String listId, Collection<String> subscribersIds) {
        subscribersIds.forEach(subscriberId -> {
            DeleteSubscriberCampaignMonitorRequest request = new DeleteSubscriberCampaignMonitorRequest(this.httpConnection.getApiKey(),
                    listId,
                    subscriberId);
            this.httpConnection.executeRequest(request);
        });
    }

    @Override
    public Map<String, String> retrieveCurrentUsersAndHashes(String listId) {
        Map<String, String> result = new HashMap<>(10000);
        IRowIterator iterator = this.httpConnection.getIterator(new DataSourceDataHolder(listId, null, Collections.emptyList(), DataSourceType.CUSTOMER, null));
        iterator.forEachRemaining(row -> {
            String email = null;
            String hash = null;

            for (DatasetValue value : row.getValues()) {
                if (EMAIL_ADDRESS_FIELD.equalsIgnoreCase(value.getAlias())) {
                    email = (String) value.getValue();
                } else if (DISTIL_HASH_FIELD.equalsIgnoreCase(value.getAlias())) {
                    hash = (String) value.getValue();
                }
            }

            if (email != null && hash != null) {
                result.put(email, hash);
            } else {
                log.info("Email or hash is null, somethings wrong in CM data. Row - {}", row);
            }
        });

        return result;
    }

    @Override
    protected Subscriber buildBaseSubscriber() {
        Subscriber result = new Subscriber();
        result.setCustomFields(new ArrayList<>());
        return result;
    }

    @Override
    protected void addCustomField(Subscriber subscriber, String fieldName, String value) {
        subscriber.getCustomFields().add(new CustomField(fieldName, value));
    }


}
