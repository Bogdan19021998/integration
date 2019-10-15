package ai.distil.integration.job.sync.http.campmon;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder;
import ai.distil.integration.job.sync.http.campmon.request.SubscribersCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.vo.SubscribersPage;
import ai.distil.integration.service.RestService;

import java.util.List;
import java.util.stream.Collectors;

public class CampaignMonitorWithCustomFieldsHttpConnection extends CampaignMonitorHttpConnection {

    private List<String> fields;

    public CampaignMonitorWithCustomFieldsHttpConnection(DTOConnection dtoConnection, RestService restService, CampaignMonitorFieldsHolder fieldsHolder, List<String> fields) {
        super(dtoConnection, restService, fieldsHolder);
        this.fields = fields;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSource, DatasetPageRequest pageRequest) {
        SubscribersCampaignMonitorRequest request = new SubscribersCampaignMonitorRequest(getConnectionSettings().getApiKey(), dataSource.getDataSourceId(), pageRequest);
        SubscribersPage subscribers = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());

        List<DatasetRow> rows = subscribers.getResults()
                .stream()
                .map(row -> new DatasetRow(fields.stream().map(field -> new DatasetValue(row.get(field), field)).collect(Collectors.toList())))
                .collect(Collectors.toList());

        return new DatasetPage(rows, null);

    }
}
