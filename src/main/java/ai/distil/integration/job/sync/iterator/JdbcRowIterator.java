package ai.distil.integration.job.sync.iterator;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.controller.dto.data.DatasetColumnType;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.JdbcConnection;
import ai.distil.integration.job.sync.jdbc.vo.QueryWrapper;
import ai.distil.integration.utils.func.BiFunctionChecked;
import com.datastax.driver.core.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class JdbcRowIterator implements IRowIterator {

    private static Map<DatasetColumnType, BiFunctionChecked<String, ResultSet, Object>> typeConverter =
            new HashMap<DatasetColumnType, BiFunctionChecked<String, ResultSet, Object>>() {{
                this.put(DatasetColumnType.STRING, (label, rs) -> rs.getString(label));
                this.put(DatasetColumnType.BOOLEAN, (label, rs) -> rs.getBoolean(label));
                this.put(DatasetColumnType.DECIMAL, (label, rs) -> rs.getLong(label));
                this.put(DatasetColumnType.DOUBLE, (label, rs) -> rs.getDouble(label));
                this.put(DatasetColumnType.FLOAT, (label, rs) -> rs.getFloat(label));
                this.put(DatasetColumnType.INTEGER, (label, rs) -> rs.getInt(label));
                this.put(DatasetColumnType.BIGINT, (label, rs) -> rs.getLong(label));
                this.put(DatasetColumnType.UUID, (label, rs) -> UUID.fromString(rs.getString(label)));
                this.put(DatasetColumnType.TIMEUUID, (label, rs) -> UUID.fromString(rs.getString(label)));
                this.put(DatasetColumnType.TIMESTAMP, (label, rs) -> rs.getTimestamp(label));
                this.put(DatasetColumnType.DATE, (label, rs) -> Optional.ofNullable(rs.getDate(label)).map(Date::getTime).map(LocalDate::fromMillisSinceEpoch).orElse(null));
                this.put(DatasetColumnType.TIME, (label, rs) -> rs.getTime(label));
                this.put(DatasetColumnType.UNKNOWN, (label, rs) -> rs.getString(label));
            }};
    private DataSourceDataHolder dataSource;
    private QueryWrapper queryWrapper;
    private JdbcConnection connection;

    //  create the row iterator with predefined getSchema
    public JdbcRowIterator(JdbcConnection connection, DataSourceDataHolder dataSource) {
        this.connection = connection;
        this.dataSource = dataSource;
    }

    @Override
    public boolean hasNext() {
        ResultSet rs = getQueryWrapper().getResultSet();

        try {
            return rs.isBeforeFirst()
                    ? rs.next()
                    : !rs.isAfterLast();
        } catch (SQLException e) {
            throw new JDBCException("Can't define has next row or not", e);
        }
    }

    @Override
    public DatasetRow next() {
        List<DatasetValue> values = new ArrayList<>();

        QueryWrapper queryWrapper = getQueryWrapper();

        List<DTODataSourceAttribute> columns = dataSource.getAllAttributes();
        try {
            ResultSet rs = queryWrapper.getResultSet();
            if (rs.isBeforeFirst()) {
                rs.next();
            }

            for (DTODataSourceAttribute column : columns) {
                String label = column.getAttributeSourceName();

                DatasetColumnType type = DatasetColumnType.mapFromSystemType(column.getCassandraAttributeType());

                Object value = getValue(type, rs, label);
                values.add(new DatasetValue(value, column.getAttributeSourceName()));
            }
            rs.next();
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw new JDBCException("Can't get the next row", (SQLException) e);
            }
            throw new RuntimeException("Can't get the next row data, unknown exception.", e);
        }

        return new DatasetRow(values);
    }

    @Override
    public DataSourceDataHolder getDataSource() {
        return this.dataSource;
    }

    private QueryWrapper getQueryWrapper() {
        if (queryWrapper == null) {
            queryWrapper = connection.queryTable(this.dataSource);
        }
        return queryWrapper;
    }

    @Override
    public void close() throws Exception {
        Optional.ofNullable(this.queryWrapper).ifPresent(QueryWrapper::close);
    }

    private Object getValue(DatasetColumnType type, ResultSet rs, String label) throws Exception {
        BiFunctionChecked<String, ResultSet, Object> converter = typeConverter.get(type);
        if (converter == null) {
            log.error("There is no converter for type - ", type);
            return null;
        }

        return converter.apply(label, rs);
    }
}
