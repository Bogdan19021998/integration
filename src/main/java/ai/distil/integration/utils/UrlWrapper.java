package ai.distil.integration.utils;

import io.netty.util.internal.StringUtil;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class UrlWrapper {

    @Getter
    private String sourceUrl;
    @Getter
    private String resultUrl;

    public UrlWrapper(String url) {
        this.sourceUrl = url;
        this.resultUrl = url;
    }

    public UrlWrapper addParam(String name, String value) {

        if (StringUtil.isNullOrEmpty(value)) {
            return this;
        }

        String separatorCharacter = this.resultUrl.contains("?") ? "&" : "?";

        this.resultUrl = this.resultUrl + separatorCharacter + name + "=" + encodeValue(value);

        return this;

    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
