package io.autoflow.plugin.openai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.List;
import java.util.stream.Collectors;

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
        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        BeanUtil.copyProperties(openAIParameter, openAiChatOptions);
        OpenAiChatClient openAiChatClient = new OpenAiChatClient(openAiApi, openAiChatOptions);
        List<Message> list = openAIParameter.getMessages().stream()
                .map(message -> new ChatMessage(message.getMessageType(), message.getContent()))
                .collect(Collectors.toList());
        ChatResponse response = openAiChatClient.call(new Prompt(list));
        return ExecutionData.builder().json(JSONUtil.parse(response)).build();
    }

}
