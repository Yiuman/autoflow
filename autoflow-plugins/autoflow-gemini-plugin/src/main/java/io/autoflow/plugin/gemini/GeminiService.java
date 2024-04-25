package io.autoflow.plugin.gemini;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.google.cloud.vertexai.VertexAI;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/4/25
 */
public class GeminiService extends BaseService<GeminiParameter> {

    @Override
    public String getName() {
        return "Gemini";
    }

    @Override
    public ExecutionData execute(GeminiParameter geminiParameter, ExecutionContext executionContext) {
        VertexAI vertexApi = new VertexAI(geminiParameter.getProjectId(), geminiParameter.getLocation());
        VertexAiGeminiChatOptions vertexAiGeminiChatOptions = new VertexAiGeminiChatOptions();
        BeanUtil.copyProperties(geminiParameter, vertexAiGeminiChatOptions);
        VertexAiGeminiChatClient vertexAiGeminiChatClient = new VertexAiGeminiChatClient(vertexApi, vertexAiGeminiChatOptions);
        List<Message> messages = geminiParameter.getMessages().stream()
                .map(message -> new ChatMessage(message.getMessageType(), message.getContent()))
                .collect(Collectors.toList());
        ChatResponse response = vertexAiGeminiChatClient.call(new Prompt(messages));
        return ExecutionData.builder().json(JSONUtil.parse(response)).build();
    }
}
