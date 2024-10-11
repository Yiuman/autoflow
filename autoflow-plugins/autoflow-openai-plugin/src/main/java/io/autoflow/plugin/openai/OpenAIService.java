package io.autoflow.plugin.openai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
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
public class OpenAIService extends BaseService<OpenAIParameter, ChatResult> {

    @Override
    public String getName() {
        return "OpenAI";
    }

    @Override
    public ChatResult execute(OpenAIParameter openAIParameter, ExecutionContext executionContext) {
        openAIParameter.setStop(CollUtil.filter(openAIParameter.getStop(), StrUtil::isNotBlank));
        OpenAiApi openAiApi = new OpenAiApi(openAIParameter.getBaseUrl(), openAIParameter.getOpenaiApiKey());
        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        BeanUtil.copyProperties(openAIParameter, openAiChatOptions);
        OpenAiChatClient openAiChatClient = new OpenAiChatClient(openAiApi, openAiChatOptions);
        List<Message> list = openAIParameter.getChatMessages().stream()
                .map(message -> new ChatMessage(MessageType.valueOf(message.getType().name()), message.getContent()))
                .collect(Collectors.toList());
        ChatResponse response = openAiChatClient.call(new Prompt(list));
        AssistantMessage output = response.getResult().getOutput();
        return new ChatResult(
                output.getMessageType(),
                output.getProperties(),
                output.getContent()
        );

    }

}
