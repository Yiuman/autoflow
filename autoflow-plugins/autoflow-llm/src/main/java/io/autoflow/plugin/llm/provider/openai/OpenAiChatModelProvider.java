package io.autoflow.plugin.llm.provider.openai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.validation.ValidationUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.spi.exception.InputValidateException;
import jakarta.validation.ConstraintViolation;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * @author yiuman
 * @date 2024/9/27
 */
public class OpenAiChatModelProvider implements ChatLanguageModelProvider {

    @Override
    public ChatModel create(ModelConfig modelConfig, Map<String, Object> parameter) {
        OpenAiParameter openAiParameter = BeanUtil.toBean(parameter, OpenAiParameter.class);
        Set<ConstraintViolation<OpenAiParameter>> validated = ValidationUtil.validate(openAiParameter);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));
        String modelName = StrUtil.blankToDefault(openAiParameter.getModelName(), modelConfig.getModelName());
        return OpenAiChatModel.builder()
                .modelName(modelName)
                .timeout(Duration.ofSeconds(openAiParameter.getTimeout()))
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
