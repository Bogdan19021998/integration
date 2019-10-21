package ai.distil.integration.utils;

import ai.distil.integration.controller.dto.data.DatasetValue;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashHelper {
    public static final Funnel<DatasetValue> DATASET_ROW_FUNNEL = (Funnel<DatasetValue>) (value, into) -> {
        into.putString(value.getAlias(), Charsets.UTF_8);
        into.putLong(value.getValue().hashCode());
    };

    public static final Funnel<String> STRING_FUNNEL = (Funnel<String>) (value, into) -> {
        into.putString(value, Charsets.UTF_8);
    };

    public static final String md5Hash(String value) {
        try {
            return DatatypeConverter.printHexBinary(
                    MessageDigest.getInstance("MD5").digest(value.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
