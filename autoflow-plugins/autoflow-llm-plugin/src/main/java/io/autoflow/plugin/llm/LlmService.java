package io.autoflow.plugin.llm;

import cn.hutool.core.date.StopWatch;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.plugin.llm.provider.ChatModelProviders;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.Linkage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yiuman
 * @date 2024/9/26
 */
@Slf4j
public class LlmService extends BaseService<LlmParameter, LlmResult> {

    @Override
    public String getName() {
        return "LLM";
    }

    @Override
    public LlmResult execute(LlmParameter llmParameter, ExecutionContext executionContext) {
        StopWatch stopWatch = new StopWatch("LLM execute");
        stopWatch.start("构建模型");
        ChatLanguageModel chatLanguageModel = buildChatLanguageModel(llmParameter);
        stopWatch.stop();
        stopWatch.start("构建消息");
        List<dev.langchain4j.data.message.ChatMessage> chatMessages = llmParameter.getMessages().stream()
                .map(ChatModelProviders::toLangChainMessage)
                .toList();
        stopWatch.stop();
        stopWatch.start("GEN");
        Response<AiMessage> response = chatLanguageModel.generate(chatMessages);
        stopWatch.stop();
        log.debug("\n" + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        return LlmResult.from(response.content().text());
    }

    private ChatLanguageModel buildChatLanguageModel(LlmParameter llmParameter) {
        Linkage<String> model = llmParameter.getModel();
        ModelConfig modelConfig = ModelParameterProvider.getModelConfigByModelName(model.getValue());
        ChatLanguageModelProvider chatLanguageModelProvider = ChatModelProviders.get(modelConfig.getImplClass());
        return chatLanguageModelProvider.create(modelConfig, model.getParameter());
    }

}
