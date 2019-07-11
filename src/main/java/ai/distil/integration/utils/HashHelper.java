package ai.distil.integration.utils;

import ai.distil.integration.controller.dto.data.DatasetValue;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;

public class HashHelper {
    public static final Funnel<DatasetValue> DATASET_ROW_FUNNEL = (Funnel<DatasetValue>) (value, into) -> {
        into.putString(value.getAlias(), Charsets.UTF_8);
        into.putLong(value.getValue().hashCode());
    };
}
