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
import ai.distil.integration.utils.RetryUtils;
import ai.distil.integration.utils.StringUtils;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.schemabuilder.*;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
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
        return RetryUtils.defaultCassandraTimeoutRetry(() -> connection.getSession().executeAsync(selectStatement).getUninterruptibly());
    }

    public void applySchemaChanges(String tenantId, @NotNull String tableName, AttributeChangeInfo changeAttributeType) {
        String keyspaceName = buildKeyspaceName(tenantId);

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

        RetryUtils.defaultCassandraTimeoutRetry(() -> this.connection.getSession().execute(deleteColumnStatement));
    }

    public void addColumn(String keyspaceName, String tableName, DTODataSourceAttribute attribute) {
        SchemaStatement addColumnStatement = SchemaBuilder.alterTable(keyspaceName, tableName)
                .addColumn(attribute.getAttributeDistilName())
                .type(DatasetColumnType.mapFromSystemType(attribute.getCassandraAttributeType()).getCassandraType());

        RetryUtils.defaultCassandraTimeoutRetry(() -> this.connection.getSession().execute(addColumnStatement));
    }

    public IngestionResult insertWithStats(String tenantId, @NotNull DataSourceDataHolder holder, DatasetRow row, Map<String, String> existingRows, boolean sync) {
        try {
            InsertStatementWrapper insertStatement = buildInsertStatement(tenantId, holder, row);
            Insert insert = insertStatement.getInsertStatement();

            IngestionStatus ingestionStatus = defineIngestionStatus(insertStatement, existingRows);

            if (IngestionStatus.CREATED.equals(ingestionStatus)) {
                insert.value(CREATED_AT_COLUMN, new Date());
            }

            return RetryUtils.defaultCassandraTimeoutRetry(() -> {

                ResultSetFuture resultSetFuture = null;

                if (!IngestionStatus.NOT_CHANGED.equals(ingestionStatus)) {
                    resultSetFuture = connection.getSession().executeAsync(insert);
                }

                if (sync && resultSetFuture != null) {
                    resultSetFuture.getUninterruptibly();
                }

                return new IngestionResult(
                        resultSetFuture,
                        ingestionStatus,
                        insertStatement.getPrimaryKey(),
                        insertStatement.getNotNullAttributesIds());
            });

        } catch (Exception e) {
            log.error("Can't ingest row", e);
            return new IngestionResult(null, IngestionStatus.ERROR, null, Collections.emptySet());
        }

    }

    public ResultSetFuture deleteFromTable(String tenantId, DataSourceDataHolder holder, String primaryKey, boolean sync) {
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();

        DTODataSourceAttribute primaryKeyColumn = holder.getPrimaryKey();

        Delete.Where delete = QueryBuilder.delete()
                .from(keyspaceName, tableName)
                .where(eq(PARTITION_COLUMN, partitionColumnValue(primaryKey)))
                .and(eq(primaryKeyColumn.getAttributeDistilName(), primaryKey));

        return RetryUtils.defaultCassandraTimeoutRetry(() -> {
            ResultSetFuture resultSetFuture = connection.getSession().executeAsync(delete);

            if (sync) {
                resultSetFuture.getUninterruptibly();
            }

            return resultSetFuture;
        });

    }

    //      key is an id, value is a hash
    public Map<String, String> getAllRowsIdsAndHashes(String tenantId, @NotNull DataSourceDataHolder holder) {
        Class<String> stringClazz = String.class;

        String keyspaceName = buildKeyspaceName(tenantId);
        String primaryKeyColumn = holder.getPrimaryKey().getAttributeDistilName();

        Select select = QueryBuilder.select(primaryKeyColumn, HASH_COLUMN)
                .from(keyspaceName, holder.getDataSourceCassandraTableName());

        ResultSet resultSet = RetryUtils.defaultCassandraTimeoutRetry(() -> connection.getSession().execute(select));

        List<Row> allRows = resultSet.all();

//      set the load factor to 1 for avoid buckets rebuilding
        Map<String, String> resultMap = new HashMap<>(allRows.size(), 1.f);

//      use plain for, because it has better performance
        for (Row row : allRows) {
            String primaryKey = row.get(primaryKeyColumn, stringClazz);
            String hash = row.get(HASH_COLUMN, stringClazz);
            resultMap.put(primaryKey, hash);
        }

        return resultMap;
    }

    public void dropTableIfExists(String tenantId, @NotNull DataSourceDataHolder holder) {
        dropTableIfExists(tenantId, holder.getDataSourceCassandraTableName());
    }

    public void dropTableIfExists(String tenantId, @NotNull String tableName) {
        String keyspaceName = buildKeyspaceName(tenantId);
        Drop dropStatement = SchemaBuilder.dropTable(keyspaceName, tableName).ifExists();

        RetryUtils.defaultCassandraTimeoutRetry(() -> this.connection.getSession().execute(dropStatement));
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
                .addClusteringColumn(primaryKey.getAttributeDistilName(), DataType.text())
                .addColumn(HASH_COLUMN, DataType.text())
                .addColumn(CREATED_AT_COLUMN, DataType.timestamp())
                .addColumn(UPDATED_AT_COLUMN, DataType.timestamp());

        holder.getAttributesWithoutPrimaryKey()
                .forEach(datasetColumn -> createSchema.addColumn(datasetColumn.getAttributeDistilName(),
                        DatasetColumnType.mapFromSystemType(datasetColumn.getCassandraAttributeType()).getCassandraType()));

        return RetryUtils.defaultCassandraTimeoutRetry(() -> {
            ResultSetFuture resultSetFuture = this.connection.getSession().executeAsync(createSchema);

            if (sync) {
                resultSetFuture.getUninterruptibly();
            }

            return resultSetFuture;
        });
    }

    public ResultSetFuture createKeySpaceIfNotExists(@NotNull String keyspaceName, boolean sync) {

        Map<String, Object> defaultReplication = connection.getAccountKeyspaceReplicationOptions();

        KeyspaceOptions keyspaceOptions = SchemaBuilder.createKeyspace(keyspaceName)
                .ifNotExists()
                .with()
                .replication(defaultReplication);

        return RetryUtils.defaultCassandraTimeoutRetry(() -> {
            ResultSetFuture resultSetFuture = this.connection.getSession().executeAsync(keyspaceOptions);

            if (sync) {
                resultSetFuture.getUninterruptibly();
            }
            return resultSetFuture;
        });

    }

    private InsertStatementWrapper buildInsertStatement(String tenantId, @NotNull DataSourceDataHolder holder, DatasetRow row) {
        Set<Long> notNullAttributesIds = new HashSet<>();

        Hasher hasher = Hashing.sha1().newHasher();
        String keyspaceName = buildKeyspaceName(tenantId);
        String tableName = holder.getDataSourceCassandraTableName();
        DTODataSourceAttribute primaryKey = holder.getPrimaryKey();

        Insert insertBuilder = QueryBuilder.insertInto(keyspaceName, tableName);

        String primaryKeyValue = null;

        Map<String, DatasetValue> valuesByKeys = ListUtils.groupByWithOverwrite(row.getValues(), DatasetValue::getAlias, false);

        for (DTODataSourceAttribute attribute : holder.getAllAttributes()) {
            DatasetValue value = valuesByKeys.get(attribute.getAttributeSourceName());

            if (value != null && value.getValue() != null) {
                Object valueForSave = value.getValue();

                notNullAttributesIds.add(attribute.getId());
                if (primaryKey.getAttributeSourceName().equals(value.getAlias())) {
                    String stringValue = valueForSave.toString();
                    primaryKeyValue = stringValue;
                    insertBuilder.value(PARTITION_COLUMN, partitionColumnValue(stringValue));
                    insertBuilder.value(primaryKey.getAttributeDistilName(), stringValue);
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
                .notNullAttributesIds(notNullAttributesIds)
                .build();
    }

    private String buildKeyspaceName(@NotNull String tenantId) {
        return KEYSPACE_PREFIX + tenantId;
    }

    private int partitionColumnValue(String hash) {
        return hash.hashCode() % DEFAULT_PARTITION_FACTOR;
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
        private Set<Long> notNullAttributesIds;
    }
}
