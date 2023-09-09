package io.autoflow.plugin.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;

import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public class HttpRequestService extends BaseService<HttpRequestParameter> {

    @Override
    public String getName() {
        return "HTTP";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<ExecutionData> execute(HttpRequestParameter httpRequestParameter) {
        String url = UrlBuilder.of(httpRequestParameter.getUrl())
                .setQuery(UrlQuery.of(httpRequestParameter.getParams(), true))
                .build();
        HttpRequest request = HttpUtil.createRequest(httpRequestParameter.getMethod(), url);
        request.addHeaders(httpRequestParameter.getHeaders());
        try (HttpResponse response = request.execute()) {
            //todo 根据不同的响应类型作处理
            return CollUtil.newArrayList(ExecutionData.builder()
                    .raw(response.body())
                    .build());
        }
    }
}
