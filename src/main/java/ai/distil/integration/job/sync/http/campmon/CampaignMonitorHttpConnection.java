package ai.distil.integration.job.sync.http.campmon;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder;
import ai.distil.integration.job.sync.http.campmon.request.*;
import ai.distil.integration.job.sync.http.campmon.vo.*;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.service.RestService;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.org.SyncSchedule;
import ai.distil.model.types.DataSourceType;
import ai.distil.model.types.SyncFrequency;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CampaignMonitorHttpConnection extends AbstractHttpConnection {

    private CampaignMonitorFieldsHolder fieldsHolder;

    public CampaignMonitorHttpConnection(DTOConnection dtoConnection, RestService restService, CampaignMonitorFieldsHolder fieldsHolder) {
        super(dtoConnection, restService, fieldsHolder);
        this.fieldsHolder = fieldsHolder;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSource, DatasetPageRequest pageRequest) {
        SubscribersCampaignMonitorRequest request = new SubscribersCampaignMonitorRequest(getConnectionSettings().getApiKey(), dataSource.getDataSourceId(), pageRequest);
        SubscribersPage subscribers = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());

        return new DatasetPage(subscribers.getResults()
                .stream()
                .map(row -> fieldsHolder.transformRow(row))
                .collect(Collectors.toList()), null);
    }

    @Override
    protected int getDefaultPageNumber() {
        return 1;
    }

    @Override
    protected String getBaseUrl() {
        return HttpConnectionConfiguration.CAMPAIGN_MONITOR.getBaseUrl();
    }

    @Override
    public boolean isAvailable() {
        ClientsCampaignMonitorRequest request = new ClientsCampaignMonitorRequest(this.getConnectionSettings().getApiKey());
        List<Client> clients = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());
        return clients != null;
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        ClientsCampaignMonitorRequest request = new ClientsCampaignMonitorRequest(this.getConnectionSettings().getApiKey());
        List<Client> clients = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());

        List<Link> lists = clients.stream()
                .map(client -> this.restService.execute(getBaseUrl(),
                        new ListsCampaignMonitorRequest(this.getConnectionSettings().getApiKey(), client.getClientId()),
                        JsonDataConverter.getInstance()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return lists.stream().map(this::mapClientToDataSource).collect(Collectors.toList());
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition sourceDefinition) {
        return this.mapClientToDataSource(new Link(sourceDefinition.getDataSourceId(), null));
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        ConnectionSettings connectionSettings = this.getConnectionSettings();
        GetSpecificListRequest request = new GetSpecificListRequest(connectionSettings.getApiKey(), dataSource.getDataSourceId());

        SpecificList list = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());

        return list != null;
    }

    private DTODataSource mapClientToDataSource(Link link) {
        SyncSchedule syncSchedule = new SyncSchedule();
        syncSchedule.setSyncFrequency(SyncFrequency.daily);

        return new DTODataSource(
                null,
                this.getConnectionData().getId(),
                link.getName(),
                null,
                link.getListId(),
                syncSchedule,
                null,
                null,
                DataSourceType.CUSTOMER,
                0,
                0,
                this.findDataSourceAttributes(link),
                generateTableName(link.getListId()),
                null
        );
    }

    private List<DTODataSourceAttribute> findDataSourceAttributes(Link link) {
        CustomListFieldsCampaignMonitorRequest request = new CustomListFieldsCampaignMonitorRequest(this.getConnectionSettings().getApiKey(), link.getListId());
        List<CustomFieldDefinition> fields = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());

        List<SimpleDataSourceField> allFields = this.fieldsHolder.getAllFields(fields);

        return allFields.stream().map(this::buildDTODataSourceAttribute).collect(Collectors.toList());
    }
}
