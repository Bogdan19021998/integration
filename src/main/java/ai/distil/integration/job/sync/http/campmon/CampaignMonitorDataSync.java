package ai.distil.integration.job.sync.http.campmon;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.job.destination.IDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder;
import ai.distil.integration.job.sync.http.campmon.request.CustomListFieldsCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateCustomFieldCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.CreateListCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateCustomFieldBody;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateListBody;
import ai.distil.integration.job.sync.http.campmon.vo.Client;
import ai.distil.integration.job.sync.http.campmon.vo.CustomFieldDefinition;
import ai.distil.integration.job.sync.http.campmon.vo.Link;
import ai.distil.integration.service.RestService;
import ai.distil.integration.utils.ConcurrentUtils;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.org.destination.DestinationIntegration;
import ai.distil.model.org.destination.DestinationIntegrationAttribute;

import java.util.*;
import java.util.stream.Collectors;

public class CampaignMonitorDataSync extends CampaignMonitorHttpConnection implements IDataSync {

    public static final String DEFAULT_UNSUBSCRIBE_SETTINGS = "AllClientLists";
    public static final String FAKE_NAME_FOR_ATTR = "FAKE_NAME";
    public static final String DEFAULT_FIELD_NAME = "Text";
    private DestinationIntegration destinationIntegration;

    public CampaignMonitorDataSync(DTOConnection dtoConnection, DestinationIntegration destinationIntegration, RestService restService, CampaignMonitorFieldsHolder fieldsHolder) {
        super(dtoConnection, restService, fieldsHolder);
        this.destinationIntegration = destinationIntegration;
    }

    @Override
    public String createListIfNotExists() {
        List<Client> clients = requestAllClients().orElse(Collections.emptyList());
        clients.sort(Comparator.comparing(Client::getClientId));

        if (clients.size() == 0) {
            throw new RuntimeException("There is no client to save data to.");
        }

        Client client = clients.get(0);

        String listName = buildListName(this.destinationIntegration.getFkDestinationId());
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

        List<DestinationIntegrationAttribute> attributes = this.destinationIntegration.getAttributes();

        List<CustomAttributeDefinition> createdAttributes = ConcurrentUtils.wait(attributes.stream().filter(attr -> {
            String fieldName = buildCustomFieldName(FAKE_NAME_FOR_ATTR, attr.getId());
            String key = currentCustomFields.get(fieldName);

            if (key == null) {
                return true;
            }

            result.add(new CustomAttributeDefinition(key, fieldName, attr.getFkDataSourceAttributeId()));
            return false;
        }).map(attr -> {
            String fieldName = buildCustomFieldName(FAKE_NAME_FOR_ATTR, attr.getId());
            CreateCustomFieldBody body = new CreateCustomFieldBody(fieldName, DEFAULT_FIELD_NAME, true);

            CreateCustomFieldCampaignMonitorRequest request = new CreateCustomFieldCampaignMonitorRequest(listId, getConnectionSettings().getApiKey(), body);
            return this.restService.executeAsync(getBaseUrl(), request, JsonDataConverter.getInstance())
                    .thenApply(key -> new CustomAttributeDefinition(key, fieldName, attr.getFkDataSourceAttributeId()));

        }).collect(Collectors.toList()));

        result.addAll(createdAttributes);

        return result;


    }

    @Override
    public void ingestData() {

    }
}
