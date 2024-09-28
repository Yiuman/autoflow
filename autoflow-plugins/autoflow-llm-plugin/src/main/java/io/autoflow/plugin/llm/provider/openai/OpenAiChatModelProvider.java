package io.autoflow.plugin.llm.provider.openai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.validation.ValidationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
public class OpenAiChatModelProvider implements ChatLanguageModelProvider {

    @Override
    public ChatLanguageModel create(Linkage<Model> modelParameter) {
        Model model = modelParameter.getValue();
        OpenAiParameter openAiParameter = BeanUtil.toBean(modelParameter.getParameter(), OpenAiParameter.class);
        Set<ConstraintViolation<OpenAiParameter>> validated = ValidationUtil.validate(openAiParameter);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));

        return OpenAiChatModel.builder()
                .modelName(model.getModelName())
                .baseUrl(openAiParameter.getBaseUrl())
                .apiKey(openAiParameter.getApiKey())
                .frequencyPenalty(openAiParameter.getFrequencyPenalty())
                .temperature(openAiParameter.getTemperature())
                .presencePenalty(openAiParameter.getPresencePenalty())
                .topP(openAiParameter.getTopP())
                .maxTokens(openAiParameter.getMaxTokens())
                .seed(openAiParameter.getSeed())
                .user(openAiParameter.getUser())
                .stop(openAiParameter.getStop())
                .responseFormat(openAiParameter.getResponseFormat())
                .build();

    }
}
