package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.request.IHttpRequest;
import com.google.common.collect.Lists;
import org.asynchttpclient.Param;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IMailChimpRequest<R> extends IHttpRequest<R> {
    String DEFAULT_COUNT_KEY = "count";
    String DEFAULT_OFFSET_KEY = "offset";

    default List<Param> buildDefaultPageParams(PageRequest pageRequest) {
        return Lists.newArrayList(
                new Param(DEFAULT_COUNT_KEY, String.valueOf(pageRequest.getPageSize())),
                new Param(DEFAULT_OFFSET_KEY, String.valueOf(pageRequest.getPageNumber() * pageRequest.getPageSize()))
        );
    }
}
