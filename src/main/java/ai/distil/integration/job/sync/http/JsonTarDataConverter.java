package ai.distil.integration.job.sync.http;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JsonTarDataConverter implements IDataConverter {

    private static JsonTarDataConverter instance;
    private static final JsonDataConverter jsonDataConverter = JsonDataConverter.getInstance();

    public static JsonTarDataConverter getInstance() {
        if (instance == null) {
            instance = new JsonTarDataConverter();
        }

        return instance;
    }

    @Override
    public <T> String toString(T t) {
        return jsonDataConverter.toString(t);
    }

    @Override
    public <T> T fromString(String t, TypeReference<T> clazz) {
        InputStream stream = new ByteArrayInputStream(t.getBytes(StandardCharsets.UTF_8));
        return parseStream(stream, clazz);
    }

    @Override
    public <T> T fromString(String t, Class<T> clazz) {
//        this is only for json conversion
        return jsonDataConverter.fromString(t, clazz);
    }

    @Override
    public <T> T fromBytes(byte[] t, TypeReference<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(t);
        return parseStream(byteArrayInputStream, clazz);
    }

    @Override
    public <T> T parseStream(InputStream is, TypeReference<T> tr) {
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(is); TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String zip = IOUtils.toString(tarIn, Charset.forName("UTF-8"));
                    return jsonDataConverter.fromString(zip, tr);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return null;
    }

}
