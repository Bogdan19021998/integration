package ai.distil.integration.cassandra.repository;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.cassandra.CassandraConnection;
import ai.distil.integration.cassandra.repository.vo.IngestionResult;
import ai.distil.integration.cassandra.repository.vo.IngestionStatus;
import ai.distil.integration.controller.dto.data.DatasetColumnType;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.controller.dto.data.DatasetValue;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.service.vo.AttributeChangeInfo;
import ai.distil.integration.utils.ListUtils;
import ai.distil.integration.utils.StringUtils;
import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.schemabuilder.*;
import com.google.common.base.Strings;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.ISODateTimeFormat;
import org.postgresql.jdbc.PgSQLXML;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.*;

import static ai.distil.integration.utils.HashHelper.DATASET_ROW_FUNNEL;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

/**
 * cassandra sync repository is responsible for store and query datasets data
 */

@Slf4j
@Repository
@RequiredArgsConstructor
public class CassandraSyncRepository {

    //  partition factor, for create a better cross nodes distribution
    public static final Integer DEFAULT_PARTITION_FACTOR = 1000;

    private static final String PARTITION_COLUMN = "p";
    private static final String PRIMARY_KEY_COLUMN = "k";
    private static final String HASH_COLUMN = "h";
    private static final String CREATED_AT_COLUMN = "c";
    private static final String UPDATED_AT_COLUMN = "u";
    public static final String KEYSPACE_PREFIX = "distil_org_";

    @Getter
    private final CassandraConnection connection;

    public List<Map<String, Object>> selectAllToMap(String tenantId, @NotNull DataSourceDataHolder holder) {
        List<Map<String, Object>> result = new ArrayList<>();

        ResultSet resultSet = selectAll(tenantId, holder);
        resultSet.forEach(row -> {
            Map<String, Object> resultRow = new TreeMap<>();
            row.getColumnDefinitions().asList().forEach(definition -> {
                resultRow.put(definition.getName(), row.getObject(definition.getName()));
            });
            result.add(resultRow);
        });
        return result;
    }


    public ResultSet selectAll(String tenantId, @NotNull DataSourceDataHolder holder) {
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        Select selectStatement = QueryBuilder.select().all().from(keyspaceName, tableName);
        return connection.getSession().executeAsync(selectStatement).getUninterruptibly();
    }

    public void applySchemaChanges(String tenantId, @NotNull DataSourceDataHolder holder, AttributeChangeInfo changeAttributeType) {
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        DTODataSourceAttribute oldAttribute = changeAttributeType.getOldAttribute();
        DTODataSourceAttribute newAttribute = changeAttributeType.getNewAttribute();

        switch (changeAttributeType.getAttributeChangeType()) {
            case NOT_CHANGED:
                break;
            case ADDED:
                addColumn(keyspaceName, tableName, newAttribute);
                break;
            case DELETED:
                deleteColumn(keyspaceName, tableName, oldAttribute);
                break;
            case TYPE_CHANGED:
                deleteColumn(keyspaceName, tableName, newAttribute);
                addColumn(keyspaceName, tableName, newAttribute);
                break;
        }

    }

    public void deleteColumn(String keyspaceName, String tableName, DTODataSourceAttribute attribute) {
        SchemaStatement deleteColumnStatement = SchemaBuilder.alterTable(keyspaceName, tableName)
                .dropColumn(attribute.getAttributeDistilName());

        this.connection.getSession().execute(deleteColumnStatement);
    }

    public void addColumn(String keyspaceName, String tableName, DTODataSourceAttribute attribute) {
        SchemaStatement addColumnStatement = SchemaBuilder.alterTable(keyspaceName, tableName)
                .addColumn(attribute.getAttributeDistilName())
                .type(DatasetColumnType.mapFromSystemType(attribute.getAttributeType()).getCassandraType());

        this.connection.getSession().execute(addColumnStatement);
    }

    public IngestionResult insertWithStats(String tenantId, @NotNull DataSourceDataHolder holder, DatasetRow row, Map<String, String> existingRows, boolean sync) {
        try {
            InsertStatementWrapper insertStatement = buildInsertStatement(tenantId, holder, row);
            Insert insert = insertStatement.getInsertStatement();
            IngestionStatus ingestionStatus = defineIngestionStatus(insertStatement, existingRows);

            if (IngestionStatus.CREATED.equals(ingestionStatus)) {
                insert.value(CREATED_AT_COLUMN, new Date());
            }

            ResultSetFuture resultSetFuture = null;

            if (!IngestionStatus.NOT_CHANGED.equals(ingestionStatus)) {
                resultSetFuture = connection.getSession().executeAsync(insert);
            }

            if (sync && resultSetFuture != null) {
                resultSetFuture.getUninterruptibly();
            }

            return new IngestionResult(resultSetFuture, ingestionStatus, insertStatement.getPrimaryKey());
        } catch (Exception e) {
            log.error("Can't ingest row", e);
            return new IngestionResult(null, IngestionStatus.ERROR, null);
        }

    }

    public ResultSetFuture deleteFromTable(String tenantId, DataSourceDataHolder holder, String primaryKey, boolean sync) {
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        Delete.Where delete = QueryBuilder.delete()
                .from(keyspaceName, tableName)
                .where(eq(PARTITION_COLUMN, partitionColumnValue(primaryKey)))
                .and(eq(PRIMARY_KEY_COLUMN, primaryKey));

        ResultSetFuture resultSetFuture = connection.getSession().executeAsync(delete);

        if (sync) {
            resultSetFuture.getUninterruptibly();
        }

        return resultSetFuture;
    }

    //      key is an id, value is a hash
    public Map<String, String> getAllRowsIdsAndHashes(String tenantId, @NotNull DataSourceDataHolder holder) {
        Class<String> stringClazz = String.class;

        String keyspaceName = buildKeyspaceName(tenantId);

        Select select = QueryBuilder.select(PRIMARY_KEY_COLUMN, HASH_COLUMN)
                .from(keyspaceName, holder.getDataSourceCassandraTableName());

        ResultSet resultSet = connection.getSession().execute(select);

        List<Row> allRows = resultSet.all();

//      set the load factor to 1 for avoid buckets rebuilding
        Map<String, String> resultMap = new HashMap<>(allRows.size(), 1.f);

//      use plain for, because it has better performance
        for (Row row : allRows) {
            String primaryKey = row.get(PRIMARY_KEY_COLUMN, stringClazz);
            String hash = row.get(HASH_COLUMN, stringClazz);
            resultMap.put(primaryKey, hash);
        }

        return resultMap;
    }

    public long getRowsCount(String tenantId, @NotNull DataSourceDataHolder holder) {
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        ResultSet resultSet = connection.getSession().execute(QueryBuilder.select().countAll().from(keyspaceName, tableName));
        Long rowsCount = resultSet.one().get(0, Long.class);

        return rowsCount == null ? 0 : rowsCount;
    }


    public void dropTableIfExists(String tenantId, @NotNull DataSourceDataHolder holder) {
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        Drop dropStatement = SchemaBuilder.dropTable(keyspaceName, tableName).ifExists();

        this.connection.getSession().execute(dropStatement);
    }

    public ResultSetFuture createTableIfNotExists(String tenantId, @NotNull DataSourceDataHolder holder, boolean sync) {
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        createKeySpaceIfNotExists(keyspaceName, true);

        DTODataSourceAttribute primaryKey = holder.getPrimaryKey();

        if (primaryKey == null) {
            throw new IllegalStateException(String.format("We can't create the table without the primary key. Organization - %s, Table - %s",
                    tenantId,
                    holder.getDataSourceCassandraTableName()));
        }

        Create createSchema = SchemaBuilder.createTable(keyspaceName, tableName)
                .ifNotExists()
                .addPartitionKey(PARTITION_COLUMN, DataType.bigint())
                .addClusteringColumn(PRIMARY_KEY_COLUMN, DataType.text())
                .addColumn(HASH_COLUMN, DataType.text())
                .addColumn(CREATED_AT_COLUMN, DataType.timestamp())
                .addColumn(UPDATED_AT_COLUMN, DataType.timestamp());


        holder.getAttributesWithoutPrimaryKey()
                .stream()
                .forEach(datasetColumn -> createSchema.addColumn(datasetColumn.getAttributeDistilName(),
                        DatasetColumnType.mapFromSystemType(datasetColumn.getAttributeType()).getCassandraType()));

        ResultSetFuture resultSetFuture = this.connection.getSession().executeAsync(createSchema);

        if (sync) {
            resultSetFuture.getUninterruptibly();
        }

        return resultSetFuture;
    }

    public ResultSetFuture createKeySpaceIfNotExists(@NotNull String keyspaceName, boolean sync) {

        Map<String, Object> defaultReplication = connection.getAccountKeyspaceReplicationOptions();

        KeyspaceOptions keyspaceOptions = SchemaBuilder.createKeyspace(keyspaceName)
                .ifNotExists()
                .with()
                .replication(defaultReplication);

        ResultSetFuture resultSetFuture = this.connection.getSession().executeAsync(keyspaceOptions);

        if (sync) {
            resultSetFuture.getUninterruptibly();
        }
        return resultSetFuture;
    }

    private InsertStatementWrapper buildInsertStatement(String tenantId, @NotNull DataSourceDataHolder holder, DatasetRow row) {
        Hasher hasher = Hashing.sha1().newHasher();
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        Insert insertBuilder = QueryBuilder.insertInto(keyspaceName, tableName);

        String primaryKeyValue = null;

        Map<String, DatasetValue> valuesByKeys = ListUtils.groupByWithOverwrite(row.getValues(), DatasetValue::getAlias, false);

        for (DTODataSourceAttribute attribute : holder.getAllAttributes()) {
            DatasetValue value = valuesByKeys.get(attribute.getAttributeSourceName());

            if (value != null && value.getValue() != null) {
                Object valueForSave = convertToCassandraType(value.getValue(), DatasetColumnType.mapFromSystemType(attribute.getAttributeType()).getCassandraType());

                if (holder.getPrimaryKey().getAttributeSourceName().equals(value.getAlias())) {
                    String stringValue = valueForSave.toString();
                    primaryKeyValue = stringValue;
                    insertBuilder.value(PARTITION_COLUMN, partitionColumnValue(stringValue));
                    insertBuilder.value(PRIMARY_KEY_COLUMN, stringValue);
                } else {
                    hasher.putObject(value, DATASET_ROW_FUNNEL);
                    insertBuilder.value(attribute.getAttributeDistilName(), valueForSave);
                }
            }
        }
        String hash = hasher.hash().toString();

        insertBuilder.value(UPDATED_AT_COLUMN, new Date());
        insertBuilder.value(HASH_COLUMN, hash);

        return InsertStatementWrapper.builder()
                .hash(hash)
                .primaryKey(primaryKeyValue)
                .insertStatement(insertBuilder)
                .build();
    }

    private String buildKeyspaceName(@NotNull String tenantId) {
        return KEYSPACE_PREFIX + tenantId;
    }

    private int partitionColumnValue(String hash) {
        return hash.hashCode() % DEFAULT_PARTITION_FACTOR;
    }

    private Object convertToCassandraType(Object object, DataType type) {
        // Convert Cassandra types - into the type they have been mapped to be stored as
        // This is especially needed for Sql.Date types, as they need to be converted to a Java.Date object

        if (object == null || Strings.isNullOrEmpty(object.toString())) {
            return null;
        }

        if (object instanceof Byte) {
            return ((Byte) object).intValue();
        } else if (object instanceof java.lang.Short) {
            return ((java.lang.Short) object).intValue();
        } else if (object instanceof java.math.BigInteger) {
            return ((java.math.BigInteger) object).longValue();
        } else if (object instanceof com.datastax.driver.core.Duration) {
            return ((com.datastax.driver.core.Duration) object).toString();
        } else if (object instanceof java.sql.Date) {
            return LocalDate.fromMillisSinceEpoch(((java.sql.Date) object).getTime());
        } else if (object instanceof java.sql.Time) {
            return ((java.sql.Time) object).getTime();
        } else if (object instanceof org.postgresql.jdbc.PgSQLXML) {
            try {
                return ((PgSQLXML) object).getString();
            } catch (SQLException e) {
                log.warn("Error getting the XML data from a org.postgresql.jdbc.PgSQLXML data type. ", e);
                return "";
            }
        }

        if (type == DataType.date()) {
            //Try and parse the date according to ISO date
            try {
                return LocalDate.fromMillisSinceEpoch(ISODateTimeFormat.dateTimeParser().parseDateTime(object.toString()).getMillis());
            } catch (Exception e) {
                log.warn("Could not parse the value '%s' into a valid Date", object, e);
                return null;
            }
        }

        return object;
    }

    private IngestionStatus defineIngestionStatus(InsertStatementWrapper wrapper, Map<String, String> existingRows) {
        String hash = existingRows.get(wrapper.getPrimaryKey());

        if (hash == null) {
            return IngestionStatus.CREATED;
        } else if (StringUtils.equals(hash, wrapper.getHash())) {
            return IngestionStatus.NOT_CHANGED;
        } else {
            return IngestionStatus.UPDATED;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class InsertStatementWrapper {
        private Insert insertStatement;
        private String primaryKey;
        private String hash;
    }
}
