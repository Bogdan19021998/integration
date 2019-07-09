package ai.distil.integration.job.sync.http;

import com.fasterxml.jackson.core.type.TypeReference;

public interface IDataConverter {
    <T> String toString(T t);
    <T> T fromString(String t, TypeReference<T> clazz);
}
