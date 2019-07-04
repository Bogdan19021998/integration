package ai.distil.integration.utils;

import ai.distil.integration.controller.dto.data.DatasetRow;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;

public class HashHelper {
    private static final Funnel<DatasetRow> DATASET_ROW_FUNNEL = (Funnel<DatasetRow>) (row, into) -> row.getValues().forEach(rowValue -> {
        if (rowValue.getValue() != null) {
            into.putString(rowValue.getAlias(), Charsets.UTF_8);
            into.putLong(rowValue.getValue().hashCode());
        }
    });

    public static String getHashForRow(DatasetRow row) {
        return Hashing.sha1().newHasher().putObject(row, DATASET_ROW_FUNNEL).hash().toString();
    }
}
