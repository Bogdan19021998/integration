package ai.distil.integration.job.sync.http.campmon;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.campmon.request.CustomListFieldsCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateCustomFieldCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateListCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.ImportSubscribersCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateCustomFieldBody;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateListBody;
import ai.distil.integration.job.sync.http.campmon.vo.*;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.ConcurrentUtils;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CampaignMonitorDataSync extends AbstractDataSync<CampaignMonitorWithCustomFieldsHttpConnection, Subscriber> {

    public static final String DEFAULT_UNSUBSCRIBE_SETTINGS = "AllClientLists";
    public static final String DEFAULT_FIELD_TYPE = "Text";

    private static final String EMAIL_ADDRESS_FIELD = "EmailAddress";

    public CampaignMonitorDataSync(DestinationIntegrationDTO destinationIntegration, List<DTODataSourceAttributeExtended> attributes, SyncSettings syncSettings,
                                   DTOConnection connection, RestService restService) {
        super(destinationIntegration, attributes, syncSettings, null);
        this.httpConnection = new CampaignMonitorWithCustomFieldsHttpConnection(connection, restService, null, Lists.newArrayList(EMAIL_ADDRESS_FIELD));
    }


    @Override
    public String createListIfNotExists() {
        List<Client> clients = this.httpConnection.requestAllClients().orElse(Collections.emptyList());
        clients.sort(Comparator.comparing(Client::getClientId));

        if (clients.size() == 0) {
            throw new RuntimeException("There is no client to save data to.");
        }

        Client client = clients.get(0);

        String listName = buildListName(this.destinationIntegration.getId());
        CreateListBody createListBody = new CreateListBody(listName, DEFAULT_UNSUBSCRIBE_SETTINGS, false);
        CreateListCampaignMonitorRequest request = new CreateListCampaignMonitorRequest(client.getClientId(), this.httpConnection.getConnectionSettings().getApiKey(), createListBody);

        return Optional.ofNullable(ConcurrentUtils.wait(this.httpConnection.requestLists(client))
                .flatMap(links -> links.stream().filter(link -> listName.equalsIgnoreCase(link.getName())).findFirst())
                .map(Link::getListId)
                .orElseGet(() -> this.httpConnection.getRestService().execute(this.httpConnection.getBaseUrl(), request, JsonDataConverter.getInstance())))
                .orElseThrow(() -> new RuntimeException(String.format("Can't find appropriate list id for list name - %s", listName)));


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

        List<CustomAttributeDefinition> createdAttributes = ConcurrentUtils.wait(retrieveAllAttributesBySettings().stream().filter(attr -> {
            String fieldName = buildCustomFieldName(attr.getAttributeSourceName(), attr.getId());
            String key = currentCustomFields.get(fieldName);

            if (key == null) {
                return true;
            }

            result.add(new CustomAttributeDefinition(key, fieldName, attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition()));
            return false;
        }).map(attr -> {
            String fieldName = buildCustomFieldName(attr.getAttributeSourceName(), attr.getId());
            CreateCustomFieldBody body = new CreateCustomFieldBody(fieldName, DEFAULT_FIELD_TYPE, true);

            CreateCustomFieldCampaignMonitorRequest request = new CreateCustomFieldCampaignMonitorRequest(listId, this.httpConnection.getConnectionSettings().getApiKey(), body);
            return this.httpConnection.getRestService()
                    .executeAsync(this.httpConnection.getBaseUrl(), request, JsonDataConverter.getInstance())
                    .thenApply(key -> new CustomAttributeDefinition(key, fieldName, attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition()));

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
    protected void removeSubscribers(List<Subscriber> subscribers) {
//        todo implement
    }

    @Override
    public Set<String> retrieveCurrentEmails(String listId) {
        Set<String> result = new HashSet<>(10000);
        IRowIterator iterator = this.httpConnection.getIterator(new DataSourceDataHolder(listId, null, Collections.emptyList(), DataSourceType.CUSTOMER, null));
        iterator.forEachRemaining(row -> row.getValues().stream()
                .filter(r -> EMAIL_ADDRESS_FIELD.equals(r.getAlias()))
                .findFirst()
                .ifPresent(v -> result.add(String.valueOf(v.getValue()))));

        return result;
    }

    @Override
    protected Subscriber getSubscriber() {
        Subscriber result = new Subscriber();
        result.setCustomFields(new ArrayList<>());
        return result;
    }

    @Override
    protected void setEmail(Subscriber subscriber, String value) {
        subscriber.setEmailAddress(value);
    }

    @Override
    protected String getEmailAddress(Subscriber subscriber) {
        return subscriber.getEmailAddress();
    }

    @Override
    protected void addCustomField(Subscriber subscriber, String fieldName, String value) {
        subscriber.getCustomFields().add(new CustomField(fieldName, value));
    }


}
