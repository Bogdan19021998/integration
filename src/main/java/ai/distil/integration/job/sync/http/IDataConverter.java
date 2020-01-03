package ai.distil.integration.job.sync.http;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;

public interface IDataConverter {
    <T> String toString(T t);

    <T> T fromString(String t, TypeReference<T> clazz);

    <T> T fromBytes(byte[] t, TypeReference<T> clazz);

    <T> T fromString(String t, Class<T> clazz);

    <T> T parseStream(InputStream is, TypeReference<T> tr);

}
