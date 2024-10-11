package io.autoflow.plugin.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.plugin.llm.provider.ChatModelProviders;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.Linkage;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/9/26
 */
public class LlmService extends BaseService<LlmParameter, LlmResult> {

    @Override
    public String getName() {
        return "LLM";
    }

    @Override
    public LlmResult execute(LlmParameter llmParameter, ExecutionContext executionContext) {
        ChatLanguageModel chatLanguageModel = buildChatLanguageModel(llmParameter);
        List<dev.langchain4j.data.message.ChatMessage> chatMessages = llmParameter.getMessages().stream()
                .map(ChatModelProviders::toLangChainMessage)
                .toList();
        Response<AiMessage> response = chatLanguageModel.generate(chatMessages);
        return LlmResult.from(response.content().text());
    }

    private ChatLanguageModel buildChatLanguageModel(LlmParameter llmParameter) {
        Linkage<String> model = llmParameter.getModel();
        ModelConfig modelConfig = ModelParameterProvider.getModelConfigByModelName(model.getValue());
        ChatLanguageModelProvider chatLanguageModelProvider = ChatModelProviders.get(modelConfig.getImplClass());
        return chatLanguageModelProvider.create(modelConfig, model.getParameter());
    }

}
