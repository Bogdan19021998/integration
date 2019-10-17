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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder.CUSTOM_FIELDS_KEY;

public class CampaignMonitorWithCustomFieldsHttpConnection extends CampaignMonitorHttpConnection {

    public static final String DEFAULT_KEY_FIELD = "Key";
    public static final String DEFAULT_VALUE_FIELD = "Value";
    private List<String> fields;
    private List<String> mergeFields;

    public CampaignMonitorWithCustomFieldsHttpConnection(DTOConnection dtoConnection, RestService restService, CampaignMonitorFieldsHolder fieldsHolder, List<String> fields, List<String> mergeFields) {
        super(dtoConnection, restService, fieldsHolder);
        this.fields = fields;
        this.mergeFields = mergeFields;
    }

    @Override
    public DatasetPage getNextPage(DataSourceDataHolder dataSource, DatasetPageRequest pageRequest) {
        SubscribersCampaignMonitorRequest request = new SubscribersCampaignMonitorRequest(getConnectionSettings().getApiKey(), dataSource.getDataSourceId(), pageRequest);
        SubscribersPage subscribers = this.restService.execute(getBaseUrl(), request, JsonDataConverter.getInstance());

        List<DatasetRow> rows = subscribers.getResults()
                .stream()
                .map(row -> {

                    List<DatasetValue> resultFields = Stream.concat(
                            fields.stream().map(field -> new DatasetValue(row.get(field), field)),
                            mergeFields.stream().map(field -> Optional.ofNullable(row.get(CUSTOM_FIELDS_KEY)).map(customFields -> {
                                List<Map<String, Object>> fields = (List<Map<String, Object>>) customFields;

                                return fields.stream().filter(customField -> field.equals(customField.get(DEFAULT_KEY_FIELD)))
                                        .findFirst()
                                        .map(fieldValue -> new DatasetValue(fieldValue.get(DEFAULT_VALUE_FIELD), field))
                                        .orElse(null);

                            }).orElse(null))).filter(Objects::nonNull).collect(Collectors.toList());

                    return resultFields.size() == 0 ? null : new DatasetRow(resultFields);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new DatasetPage(rows, null);

    }
}
