package ai.distil.integration.controller.dto.data;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetRow {
    private List<DatasetValue> values;


    public static class DatasetRowBuilder {
        private DatasetRow row = new DatasetRow(Lists.newArrayList());

        public DatasetRowBuilder addValue(String alias, Object value, DatasetColumnType datasetColumnType) {
            row.getValues().add(new DatasetValue(value, alias, datasetColumnType));
            return this;
        }

        public DatasetRowBuilder addValue(String alias, Object value) {
            row.getValues().add(new DatasetValue(value, alias, DatasetColumnType.simplifyJavaType(value)));
            return this;
        }

        public DatasetRow build() {
            return row;
        }
    }
}
