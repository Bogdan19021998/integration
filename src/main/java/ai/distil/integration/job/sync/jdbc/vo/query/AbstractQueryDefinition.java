package ai.distil.integration.job.sync.jdbc.vo.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Data
@AllArgsConstructor
public abstract class AbstractQueryDefinition<R> {

    public abstract List<Object> getQueryParams();

    public abstract R mapResultSet(ResultSet resultSet) throws SQLException;

    public abstract String getQuery();
}
