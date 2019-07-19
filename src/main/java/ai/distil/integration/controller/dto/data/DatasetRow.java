package ai.distil.integration.controller.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetRow {
    private List<DatasetValue> values;


    public static class DatasetRowBuilder {
        private final DatasetRow row;

        public DatasetRowBuilder() {
            this(8);
        }

        public DatasetRowBuilder(Integer initialSize) {
            this.row = new DatasetRow(new ArrayList<>(initialSize));
        }

        public DatasetRowBuilder addValue(String alias, Object value) {
            row.getValues().add(new DatasetValue(value, alias));
            return this;
        }

        public DatasetRow build() {
            return row;
        }
    }
}
