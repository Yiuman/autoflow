package io.autoflow.plugin.llm.provider;

import cn.hutool.core.util.ReflectUtil;

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
}
