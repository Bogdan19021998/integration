package ai.distil.integration.cassandra;

import ai.distil.integration.cassandra.codec.DateCodec;
import ai.distil.integration.controller.dto.data.DatasetColumnType;
import ai.distil.model.types.SyncFrequency;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import com.datastax.driver.mapping.MappingManager;

import java.sql.Date;
import java.sql.JDBCType;

public class CassandraMappings {
    private static final EnumNameCodec<?>[] ENUM_NAME_CODECS =
            {
                    new EnumNameCodec<>(DatasetColumnType.class),
                    new EnumNameCodec<>(JDBCType.class),
                    new EnumNameCodec<>(SyncFrequency.class)
            };

    private static final TypeCodec<?>[] TYPE_CODECS = {
            new DateCodec(TypeCodec.date(), Date.class)
    };

    private static final Class[] UDT_CLASSES =
            {
            };

    public static void map(CassandraConnection connection) {
        // map ENUMs
        for (EnumNameCodec<?> codec : ENUM_NAME_CODECS) {
            connection.registerCodec(codec);
        }

        // map UDTs
        MappingManager mapping = new MappingManager(connection.getSession());

        for (Class<?> c : UDT_CLASSES) {
            TypeCodec<?> m = mapping.udtCodec(c);
            connection.registerCodec(m);
        }

        for (TypeCodec<?> typeCodec : TYPE_CODECS) {
            connection.registerCodec(typeCodec);
        }
    }
}