package io.autoflow.plugin.llm.provider.gemini;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.validation.ValidationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.autoflow.plugin.llm.Model;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.spi.exception.InputValidateException;
import io.autoflow.spi.model.Linkage;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * @author yiuman
 * @date 2024/9/27
 */
public class GeminiModelProvider implements ChatLanguageModelProvider {

    @Override
    public ChatLanguageModel create(Linkage<Model> modelParameter) {
        Model model = modelParameter.getValue();
        GeminiParameter geminiParameter = BeanUtil.toBean(modelParameter.getParameter(), GeminiParameter.class);
        Set<ConstraintViolation<GeminiParameter>> validated = ValidationUtil.validate(geminiParameter);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));

        return GoogleAiGeminiChatModel.builder()
                .modelName(model.getModelName())
                .apiKey(geminiParameter.getApiKey())
                .candidateCount(geminiParameter.getCandidateCount())
                .stopSequences(geminiParameter.getStopSequences())
                .maxOutputTokens(geminiParameter.getMaxOutputTokens())
                .topK(geminiParameter.getTopK())
                .topP(geminiParameter.getTopP())
                .temperature(geminiParameter.getTemperature())
                .build();
    }
}
