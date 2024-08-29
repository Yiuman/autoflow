package io.autoflow.plugin.gemini;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;

/**
 * @author yiuman
 * @date 2024/4/25
 */
public class GeminiService extends BaseService<GeminiParameter, GeminiResult> {

    @Override
    public String getName() {
        return "Gemini";
    }

    @Override
    public GeminiResult execute(GeminiParameter geminiParameter, ExecutionContext executionContext) {
        String requestUrl = StrUtil.format(
                "{}/v1/models/{}:generateContent?key={}",
                geminiParameter.getBaseUrl(),
                geminiParameter.getModel(),
                geminiParameter.getApiKey()
        );
        try (HttpResponse response = HttpUtil.createPost(requestUrl)
                .body(JSONUtil.toJsonStr(new GeminiTextRequest(geminiParameter.getMessage())))
                .execute()) {
            JSON json = JSONUtil.parse(response.body());
            return new GeminiResult(
                    json.getByPath("candidates[0].content.parts[0].text", String.class)
            );
        }
    }
}
