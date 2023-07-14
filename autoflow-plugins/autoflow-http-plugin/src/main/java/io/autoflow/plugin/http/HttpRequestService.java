package io.autoflow.plugin.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.Property;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public class HttpRequestService implements Service {

    @Override
    public String getName() {
        return "HTTP";
    }

    @Override
    public List<Property> getProperties() {
        return null;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<ExecutionData> execute(ExecutionContext executionContext) {
        Map<String, List<ExecutionData>> inputData = executionContext.getInputData();
        Map<String, Object> parameter = executionContext.getParameters();
        List<ExecutionData> inputNames = inputData.get(parameter.get(Constants.INPUT_NAME));
        ExecutionData nodeInputData = CollUtil.get(inputNames, (Integer) parameter.get(Constants.INPUT_INDEX));
        JSONObject json = nodeInputData.getJson();

        HttpRequestParameter httpRequestParameter = json.to(HttpRequestParameter.class);
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
