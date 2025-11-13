package io.autoflow.plugin.llm.provider;

import dev.langchain4j.model.chat.ChatModel;
import io.autoflow.plugin.llm.ModelConfig;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/9/27
 */
public interface ChatLanguageModelProvider {

    ChatModel create(ModelConfig modelConfig, Map<String, Object> parameter);
}