package io.autoflow.plugin.llm.provider;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.autoflow.plugin.llm.Model;
import io.autoflow.spi.model.Linkage;

/**
 * @author yiuman
 * @date 2024/9/27
 */
public interface ChatLanguageModelProvider {

    ChatLanguageModel create(Linkage<Model> modelParameter);
}