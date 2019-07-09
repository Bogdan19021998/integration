package ai.distil.integration.job.sync.http;

import ai.distil.integration.exception.ConverterException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class JsonDataConverter implements IDataConverter {
    private ObjectMapper mapper;

    private static JsonDataConverter instance;

    private JsonDataConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public static JsonDataConverter getInstance() {

        if (instance == null) {
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.USE_LONG_FOR_INTS, true)
                    .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                    .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false)
                    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
            instance = new JsonDataConverter(mapper);
        }

        return instance;
    }

    @Override
    public <T> String toString(T t) {
        try {
            return mapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new ConverterException(true, "Can't serialize object to string.", e);
        }
    }

    @Override
    public <T> T fromString(String t, TypeReference<T> clazz) {
        try {
            return mapper.readValue(t, clazz);
        } catch (IOException e) {
            throw new ConverterException(false, "Can't deserialize json object", e);
        }
    }
}
