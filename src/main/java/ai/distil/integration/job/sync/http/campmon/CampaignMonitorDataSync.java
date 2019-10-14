package ai.distil.integration.job.sync.http.campmon;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.destination.IDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder;
import ai.distil.integration.job.sync.http.campmon.request.CustomListFieldsCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.SubscribersCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateCustomFieldCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateListCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.ImportSubscribersCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateCustomFieldBody;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateListBody;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.SubscribersImportResponse;
import ai.distil.integration.job.sync.http.campmon.vo.*;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.ConcurrentUtils;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.org.CustomerRecord;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static ai.distil.model.types.DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS;

@Slf4j
public class CampaignMonitorDataSync extends CampaignMonitorHttpConnection implements IDataSync {

    public static final String DEFAULT_UNSUBSCRIBE_SETTINGS = "AllClientLists";
    public static final String DEFAULT_FIELD_TYPE = "Text";
    public static final String EMAIL_ADDRESS_FIELD = "EmailAddress";

    private DestinationIntegrationDTO destinationIntegration;
    private List<DTODataSourceAttributeExtended> attributes;
    private SyncSettings syncSettings;

    public CampaignMonitorDataSync(DTOConnection dtoConnection,
                                   DestinationIntegrationDTO destinationIntegration, RestService restService,
                                   CampaignMonitorFieldsHolder fieldsHolder,
                                   List<DTODataSourceAttributeExtended> attributes, SyncSettings syncSettings) {
        super(dtoConnection, restService, fieldsHolder);

        this.destinationIntegration = destinationIntegration;
        this.attributes = attributes;
        this.syncSettings = syncSettings;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSource, DatasetPageRequest pageRequest) {
        SubscribersCampaignMonitorRequest request = new SubscribersCampaignMonitorRequest(getConnectionSettings().getApiKey(), dataSource.getDataSourceId(), pageRequest);
        SubscribersPage subscribers = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());

        List<DatasetRow> rows = subscribers.getResults()
                .stream()
                .map(row -> new DatasetRow(Collections.singletonList(new DatasetValue(row.get(EMAIL_ADDRESS_FIELD), EMAIL_ADDRESS_FIELD))))
                .collect(Collectors.toList());

        return new DatasetPage(rows, null);

    }

    @Override
    public String createListIfNotExists() {
        List<Client> clients = requestAllClients().orElse(Collections.emptyList());
        clients.sort(Comparator.comparing(Client::getClientId));

        if (clients.size() == 0) {
            throw new RuntimeException("There is no client to save data to.");
        }

        Client client = clients.get(0);

        String listName = buildListName(this.destinationIntegration.getId());
        CreateListBody createListBody = new CreateListBody(listName, DEFAULT_UNSUBSCRIBE_SETTINGS, false);
        CreateListCampaignMonitorRequest request = new CreateListCampaignMonitorRequest(client.getClientId(), this.getConnectionSettings().getApiKey(), createListBody);

        return Optional.ofNullable(ConcurrentUtils.wait(requestLists(client))
                .flatMap(links -> links.stream().filter(link -> listName.equalsIgnoreCase(link.getName())).findFirst())
                .map(Link::getListId)
                .orElseGet(() -> this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance())))
                .orElseThrow(() -> new RuntimeException(String.format("Can't find appropriate list id for list name - %s", listName)));


    }

    @Override
    public List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId) {

        List<CustomAttributeDefinition> result = new ArrayList<>();

        List<CustomFieldDefinition> customFieldDefinitions = Optional.ofNullable(this.restService.execute(getBaseUrl(),
                new CustomListFieldsCampaignMonitorRequest(this.getConnectionSettings().getApiKey(), listId),
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

            CreateCustomFieldCampaignMonitorRequest request = new CreateCustomFieldCampaignMonitorRequest(listId, getConnectionSettings().getApiKey(), body);
            return this.restService.executeAsync(getBaseUrl(), request, JsonDataConverter.getInstance())
                    .thenApply(key -> new CustomAttributeDefinition(key, fieldName, attr.getAttributeDataTag(), attr.getId(), attr.getAutoGenerated(), attr.getPosition()));

        }).collect(Collectors.toList()));

        result.addAll(createdAttributes);

        return result;

    }

    @Override
    public void ingestData(String listId, List<CustomAttributeDefinition> attributes, List<CustomerRecord> data) {

//        sort attributes first
        attributes.sort(Comparator.comparing(CustomAttributeDefinition::getPosition));

        Map<DataSourceSchemaAttributeTag, Set<String>> attributesToBackfill = ListUtils.groupByToLinkedSet(this.attributes.stream()
                        .filter(DTODataSourceAttributeExtended::getAutoGenerated)
                        .sorted(Comparator.comparingInt(DTODataSourceAttributeExtended::getPosition)).collect(Collectors.toList()),
                DTODataSourceAttributeExtended::getAttributeDataTag, dsa -> String.valueOf(dsa.getId()));

        List<String> emails = retrieveCurrentEmails(listId);

        List<Subscriber> subscribers = new ArrayList<>();

        data.forEach(record -> subscribers.add(generateSubscriberData(attributes, record, attributesToBackfill)));

        ImportSubscribersCampaignMonitorRequest importRequest = new ImportSubscribersCampaignMonitorRequest(this.getConnectionSettings().getApiKey(), listId, new Subscribers(subscribers));
        SubscribersImportResponse response = this.restService.execute(getBaseUrl(), importRequest, JsonDataConverter.getInstance());

        log.info("Data ingestion result {}", response);

    }

    @Override
    public List<String> retrieveCurrentEmails(String listId) {
        List<String> result = new ArrayList<>(10000);
        IRowIterator iterator = this.getIterator(new DataSourceDataHolder(listId, null, Collections.emptyList(), DataSourceType.CUSTOMER, null));
        iterator.forEachRemaining(row -> row.getValues().stream()
                .filter(r -> EMAIL_ADDRESS_FIELD.equals(r.getAlias()))
                .findFirst()
                .ifPresent(v -> result.add(String.valueOf(v.getValue()))));

        return result;
    }

    //  todo generalize, keep it like this for now
    private List<DTODataSourceAttributeExtended> retrieveAllAttributesBySettings() {
        Integer defaultProductsCount = this.syncSettings.getDefaultProductsCount();

        return this.attributes.stream()
                .filter(attr -> !attr.getAutoGenerated() || attr.getPosition() <= defaultProductsCount)
                .collect(Collectors.toList());
    }

    private Subscriber generateSubscriberData(List<CustomAttributeDefinition> attributes, CustomerRecord data, Map<DataSourceSchemaAttributeTag, Set<String>> values) {
        Set<String> attributesProcessed = new HashSet<>();

        ObjectNode customerValues = data.getCustomerValues();

        Subscriber result = new Subscriber();
        result.setCustomFields(new ArrayList<>());

        for (CustomAttributeDefinition attr : attributes) {

            String distilAttributeId = String.valueOf(attr.getDistilAttributeId());

            attributesProcessed.add(distilAttributeId);

            if (CUSTOMER_EMAIL_ADDRESS.equals(attr.getTag())) {
                result.setEmailAddress(customerValues.get(distilAttributeId).asText());
            } else {
                Optional<JsonNode> jsonNode = Optional.ofNullable(customerValues.get(distilAttributeId));

                if (jsonNode.map(JsonNode::isNull).orElse(true) && Optional.ofNullable(attr.getAutoGeneratedAttribute()).orElse(false)) {
                    Set<String> fieldsToBackfill = new HashSet<>(values.getOrDefault(attr.getTag(), Collections.emptySet()));
                    fieldsToBackfill.removeAll(attributesProcessed);

                    for (String s : fieldsToBackfill) {
                        attributesProcessed.add(s);
                        Optional<JsonNode> node = Optional.ofNullable(customerValues.get(s));

                        if (node.isPresent() && !node.get().isNull()) {
                            result.getCustomFields().add(new CustomField(attr.getName(), node.get().asText()));
                            break;
                        }
                    }

                } else {
                    jsonNode.ifPresent(node -> result.getCustomFields().add(new CustomField(attr.getName(), node.asText())));
                }
            }
        }

        return result;

    }

}
