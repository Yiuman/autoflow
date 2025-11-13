package io.autoflow.plugin.llm.provider.gemini;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.validation.ValidationUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.spi.exception.InputValidateException;
import jakarta.validation.ConstraintViolation;

import java.util.Map;
import java.util.Set;

/**
 * @author yiuman
 * @date 2024/9/27
 */
public class GeminiModelProvider implements ChatLanguageModelProvider {

    @Override
    public ChatModel create(ModelConfig modelConfig, Map<String, Object> parameter) {
        GeminiParameter geminiParameter = BeanUtil.toBean(parameter, GeminiParameter.class);
        Set<ConstraintViolation<GeminiParameter>> validated = ValidationUtil.validate(geminiParameter);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));

        return GoogleAiGeminiChatModel.builder()
                .modelName(modelConfig.getModelName())
                .apiKey(geminiParameter.getApiKey())
                .stopSequences(geminiParameter.getStopSequences())
                .maxOutputTokens(geminiParameter.getMaxOutputTokens())
                .topK(geminiParameter.getTopK())
                .topP(geminiParameter.getTopP())
                .temperature(geminiParameter.getTemperature())
                .build();
    }
}
