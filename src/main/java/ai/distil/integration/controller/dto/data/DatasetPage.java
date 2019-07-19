package ai.distil.integration.controller.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetPage {
    private List<DatasetRow> rows;
    private String nextPageUrl;
}
