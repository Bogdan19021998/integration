package ai.distil.integration.controller.dto.data;

import ai.distil.model.types.DataSourceAttributeType;
import com.datastax.driver.core.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@AllArgsConstructor
public enum DatasetColumnType {
    STRING(DataType.text()),
    BOOLEAN(DataType.cboolean()),
    DECIMAL(DataType.decimal()),
    DOUBLE(DataType.cdouble()),
    FLOAT(DataType.cfloat()),
    INTEGER(DataType.bigint()),
    BIGINT(DataType.bigint()),
    UUID(DataType.uuid()),
    TIMEUUID(DataType.uuid()),
    TIMESTAMP(DataType.timestamp()),
    DATE(DataType.date()),
    TIME(DataType.time()),
    UNKNOWN(DataType.text());

    @Getter
    private DataType cassandraType;

    public static DatasetColumnType mapFromSystemType(DataSourceAttributeType attributeType) {
        return DatasetColumnType.valueOf(attributeType.toString());
    }

    public static DatasetColumnType simplifyJdbcType(JDBCType type, boolean unsigned) {
        switch (type) {
            case BOOLEAN:
            case BIT:
                return DatasetColumnType.BOOLEAN;
            case TINYINT:
            case SMALLINT:
                return DatasetColumnType.INTEGER;

            case INTEGER:
                //Integer is set to BIGINT too - as it can be an unsigned integer, which can push it into the bounds of a Long
                if (unsigned) {
                    return DatasetColumnType.BIGINT;
                } else {
                    return DatasetColumnType.INTEGER;
                }
            case BIGINT:
                return DatasetColumnType.BIGINT;
            case FLOAT:
            case REAL:
                return DatasetColumnType.FLOAT;
            case DOUBLE:
                return DatasetColumnType.DOUBLE;
            case NUMERIC:
            case DECIMAL:
                return DatasetColumnType.DECIMAL;
            case CHAR:
            case VARCHAR:
            case LONGVARCHAR:
            case NCHAR:
            case NVARCHAR:
            case LONGNVARCHAR:
            case SQLXML:
                return DatasetColumnType.STRING;
            case DATE:
                return DatasetColumnType.DATE;
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                return DatasetColumnType.TIMESTAMP;
            case TIME:
            case TIME_WITH_TIMEZONE:
                return DatasetColumnType.TIME;
            case BINARY:
                break;
            case VARBINARY:
                break;
            case LONGVARBINARY:
                break;
            case NULL:
                break;
            case OTHER:
                break;
            case JAVA_OBJECT:
                break;
            case DISTINCT:
                break;
            case STRUCT:
                break;
            case ARRAY:
                break;
            case BLOB:
                break;
            case CLOB:
                break;
            case REF:
                break;
            case DATALINK:
                break;
            case ROWID:
                break;
            case REF_CURSOR:
                break;
            case NCLOB:
                break;
        }

        return DatasetColumnType.UNKNOWN;
    }

    public static DatasetColumnType simplifyJavaType(Class<?> type) {
        if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            return DatasetColumnType.BOOLEAN;
        } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
            return DatasetColumnType.INTEGER;
        } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
            return DatasetColumnType.BIGINT;
        } else if (Date.class.isAssignableFrom(type) || LocalDate.class.isAssignableFrom(type)) {
            return DatasetColumnType.DATE;
        } else if (Timestamp.class.isAssignableFrom(type) || LocalDateTime.class.isAssignableFrom(type)) {
            return DatasetColumnType.TIMESTAMP;
        } else if (type == String.class) {
            return DatasetColumnType.STRING;
        }

        return DatasetColumnType.UNKNOWN;
    }

    public static DatasetColumnType simplifyType(String type) {
        switch (type.toLowerCase()) {
            case "char":
            case "text":
            case "blob":
            case "character":
            case "varchar":
            case "longtext":
            case "mediumtext":
            case "mediumblob":
            case "longblob":
            case "character varying":
            case "nchar":
            case "nvarchar":
                return DatasetColumnType.STRING;
            case "bit":
            case "bool":
            case "boolean":
                return DatasetColumnType.BOOLEAN;
            case "int":
            case "smallint":
            case "tinyint":
            case "int2":
            case "int4":
            case "bytea":
                return DatasetColumnType.INTEGER;
            case "integet":
            case "int8":
            case "bigint":
                return DatasetColumnType.BIGINT;
            case "money":
            case "numeric":
            case "decimal":
                //Postgres (numeric) > Java (BigDecimal) > Cassandra (Decimal)
                return DatasetColumnType.DECIMAL;
            case "float4":
            case "float":
                return DatasetColumnType.FLOAT;
            case "float8":
            case "double":
            case "real":
            case "double precision":
                return DatasetColumnType.DOUBLE;
            case "time":
            case "abstime":
            case "timetz":
                return DatasetColumnType.TIME;
            case "timestamp":
            case "timestamp without time zone":
            case "timestamp with time zone":
            case "timestamptz":
                return DatasetColumnType.TIMESTAMP;
            case "uuid":
                return DatasetColumnType.UUID;
            case "date":
            case "datetime":
                return DatasetColumnType.DATE;
            //Do not natively support - convert to string
            case "xml":
            case "inet":
            case "cidr":
            case "macaddr":
                return DatasetColumnType.STRING;

            // All others
            default:
                return DatasetColumnType.UNKNOWN;
        }
    }

    public static DatasetColumnType simplifySalesforceType(String type) {
        switch (type.toLowerCase()) {
            case "xsd:boolean":
                return DatasetColumnType.BOOLEAN;

            case "xsd:double":
                return DatasetColumnType.DOUBLE;

            case "xsd:decimal":
                return DatasetColumnType.DECIMAL;

            case "xsd:float":
                return DatasetColumnType.FLOAT;

            case "xsd:byte":
            case "xsd:int":
            case "xsd:integer":
            case "xsd:short":
            case "xsd:signedInt":
            case "xsd:unsignedShort":
                return DatasetColumnType.INTEGER;

            case "tns:ID":
            case "xsd:long":
            case "xsd:unsignedInt":
                return DatasetColumnType.BIGINT;

            case "xsd:date":
                return DatasetColumnType.DATE;

            case "xsd:dateTime":
                return DatasetColumnType.TIMESTAMP;

            //Everything else - string
            default:
                return DatasetColumnType.STRING;
        }
    }

    public static boolean valueIsCorrectAccordingToType(Object value, DatasetColumnType type) {
        switch (type) {
            case STRING:
                return value instanceof String;
            case BOOLEAN:
                return value instanceof Boolean;
            case DECIMAL:
                return value instanceof BigDecimal;
            case DOUBLE:
                return value instanceof Double;
            case FLOAT:
                return value instanceof Float;
            case INTEGER:
                return value instanceof Integer;
            case BIGINT:
                return value instanceof BigInteger;
            case UUID:
            case TIMEUUID:
                return value instanceof java.util.UUID;
            case TIMESTAMP:
                return value instanceof Timestamp;
            case DATE:
                return value instanceof Date;
            case TIME:
                return value instanceof Time;

            case UNKNOWN:
            default:
                return false;
        }
    }

    public DataSourceAttributeType mapToAttributeType() {
        return DataSourceAttributeType.valueOf(this.toString());
    }
}
