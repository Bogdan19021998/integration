package ai.distil.integration.job.sync.jdbc.vo;

import ai.distil.integration.utils.func.FunctionChecked;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryWrapper implements AutoCloseable {
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;

    @Override
    public void close() {
        DbUtils.closeQuietly(resultSet);
        DbUtils.closeQuietly(statement);
        DbUtils.closeQuietly(resultSet);
    }

    public <T> List<T> readResult(FunctionChecked<ResultSet, T> mapper) throws Exception {
        List<T> result = new ArrayList<>();

        while (this.resultSet.next()) {
            result.add(mapper.apply(this.resultSet));
        }

        return result;
    }
}
