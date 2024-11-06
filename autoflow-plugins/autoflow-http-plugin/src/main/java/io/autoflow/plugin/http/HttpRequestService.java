package io.autoflow.plugin.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.FileData;
import io.autoflow.spi.model.NamedValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public class HttpRequestService extends BaseService<HttpRequestParameter, HttpResult> {

    @Override
    public String getName() {
        return "HTTP";
    }

    private static String extractFilename(String contentDisposition) {
        if (StrUtil.isBlank(contentDisposition)) {
            return null;
        }
        String fileName = null;
        String[] parts = contentDisposition.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("filename")) {
                String[] fileNameParts = part.split("=");
                fileName = fileNameParts[1].trim().replace("\"", "");
            }
        }
        return fileName;
    }

    @Override
    public HttpResult execute(HttpRequestParameter httpRequestParameter, ExecutionContext ctx) {
        String url = UrlBuilder.of(httpRequestParameter.getUrl())
                .setQuery(buildUrlQuery(httpRequestParameter.getParams()))
                .build();
        HttpRequest request = HttpUtil.createRequest(httpRequestParameter.getMethod(), url);
        List<NamedValue<String>> headers = httpRequestParameter.getHeaders();
        if (CollUtil.isNotEmpty(headers)) {
            for (NamedValue<String> header : headers) {
                request.header(header.getName(), header.getValue());
            }
        }

        try (HttpResponse response = request.execute()) {
            HttpResult httpResult = toHttpResult(response);
            String contentType = response.header(Header.CONTENT_TYPE);
            String contentDisposition = response.header(Header.CONTENT_DISPOSITION);
            String filename = extractFilename(contentDisposition);
            boolean isBinary = (StrUtil.isNotBlank(contentType)
                    && Constants.BINARY_CONTENT_TYPES.stream().anyMatch(contentType::startsWith))
                    || StrUtil.isNotBlank(filename);
            if (isBinary) {
                filename = StrUtil.isBlank(filename) ? FileUtil.getName(request.getUrl()) : filename;
                httpResult.setFileData(new FileData(
                        filename,
                        response.bodyBytes()
                ));
            }

            return httpResult;
        }
    }

    private UrlQuery buildUrlQuery(List<NamedValue<Object>> params) {
        UrlQuery urlQuery = new UrlQuery();
        if (CollUtil.isNotEmpty(params)) {
            for (NamedValue<Object> param : params) {
                if (StrUtil.isBlank(param.getName())) {
                    continue;
                }
                urlQuery.add(param.getName(), param.getValue());
            }
        }
        return urlQuery;
    }

    private HttpResult toHttpResult(HttpResponse response) {
        HttpResult httpResult = new HttpResult();
        httpResult.setStatus(response.getStatus());
        String body = response.body();
        httpResult.setBody(JSONUtil.isTypeJSON(body) ? JSONUtil.parse(body) : body);
        Map<String, List<String>> headers = new HashMap<>(response.headers());
        headers.remove(null);
        httpResult.setHeaders(headers);
        return httpResult;
    }
}
