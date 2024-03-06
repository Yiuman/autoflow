package io.autoflow.plugin.http;

import cn.hutool.core.codec.Base64Encoder;
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
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.Binary;
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
    public List<ExecutionData> execute(HttpRequestParameter httpRequestParameter) {
        String url = UrlBuilder.of(httpRequestParameter.getUrl())
                .setQuery(UrlQuery.of(httpRequestParameter.getParams(), true))
                .build();
        HttpRequest request = HttpUtil.createRequest(httpRequestParameter.getMethod(), url);
        request.addHeaders(httpRequestParameter.getHeaders());
        try (HttpResponse response = request.execute()) {
            HttpResult httpResult = toHttpResult(response);
            ExecutionData executionData = ExecutionData.builder()
                    .raw(httpResult.getBody())
                    .json(JSONUtil.parseObj(httpResult))
                    .build();

            String contentType = response.header(Header.CONTENT_TYPE);
            String contentDisposition = response.header(Header.CONTENT_DISPOSITION);
            String filename = extractFilename(contentDisposition);
            boolean isBinary = (StrUtil.isNotBlank(contentType) && Constants.BINARY_CONTENT_TYPES.stream().anyMatch(contentType::startsWith))
                    || StrUtil.isNotBlank(filename);
            if (isBinary) {
                filename = StrUtil.isBlank(filename) ? FileUtil.getName(request.getUrl()) : filename;
                executionData.setBinary(
                        new Binary(
                                filename,
                                Base64Encoder.encode(response.bodyBytes())
                        )
                );
            }

            return CollUtil.newArrayList(executionData);
        }
    }

    private HttpResult toHttpResult(HttpResponse response) {
        HttpResult httpResult = new HttpResult();
        httpResult.setStatus(response.getStatus());
        httpResult.setBody(response.body());
        httpResult.setHeaders(response.headers());
        return httpResult;
    }
}
