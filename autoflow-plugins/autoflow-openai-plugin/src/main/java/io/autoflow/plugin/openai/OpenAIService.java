package io.autoflow.plugin.openai;

import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * @author yiuman
 * @date 2024/4/24
 */
public class OpenAIService extends BaseService<OpenAIParameter> {

    @Override
    public String getName() {
        return "OpenAI";
    }

    @Override
    public ExecutionData execute(OpenAIParameter openAIParameter, ExecutionContext executionContext) {
        OpenAiApi openAiApi = new OpenAiApi(openAIParameter.getBaseUrl(), openAIParameter.getOpenaiApiKey());
        OpenAiChatClient openAiChatClient = new OpenAiChatClient(openAiApi, OpenAiChatOptions.builder()
                .withModel(openAIParameter.getModel())
                .withTemperature(0.4f)
                .withMaxTokens(200)
                .build());

        ChatResponse response = openAiChatClient.call(new Prompt("Generate the names of 5 famous pirates."));
        return ExecutionData.builder().json(JSONUtil.parse(response)).build();
    }
}
