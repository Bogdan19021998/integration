package ai.distil.integration.controller.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetValue {
    private Object value;
    private String alias;
    private DatasetColumnType columnType;
}
