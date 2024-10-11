package io.autoflow.plugin.llm.provider;

import cn.hutool.core.util.ReflectUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import io.autoflow.spi.model.ChatMessage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiuman
 * @date 2024/9/27
 */
public final class ChatModelProviders {
    private static final Map<String, ChatLanguageModelProvider> CLASS_NAME_CHAT_MODEL_PROVIDER_MAP = new ConcurrentHashMap<>();

    public static ChatLanguageModelProvider get(String className) {
        ChatLanguageModelProvider chatLanguageModelProvider = CLASS_NAME_CHAT_MODEL_PROVIDER_MAP.get(className);
        if (Objects.isNull(chatLanguageModelProvider)) {
            chatLanguageModelProvider = ReflectUtil.newInstance(className);
            CLASS_NAME_CHAT_MODEL_PROVIDER_MAP.put(className, chatLanguageModelProvider);
        }
        return chatLanguageModelProvider;
    }

    public static dev.langchain4j.data.message.ChatMessage toLangChainMessage(ChatMessage chatMessage) {
        return switch (chatMessage.getType()) {
            case USER -> UserMessage.from(chatMessage.getContent());
            case ASSISTANT -> AiMessage.from(chatMessage.getContent());
            default -> SystemMessage.from(chatMessage.getContent());
        };
    }
}
