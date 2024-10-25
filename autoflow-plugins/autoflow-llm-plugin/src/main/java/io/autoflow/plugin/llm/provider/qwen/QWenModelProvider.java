package io.autoflow.plugin.llm.provider.qwen;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.validation.ValidationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.spi.exception.InputValidateException;
import jakarta.validation.ConstraintViolation;

import java.util.Map;
import java.util.Set;

/**
 * @author yiuman
 * @date 2024/10/25
 */
public class QWenModelProvider implements ChatLanguageModelProvider {

    @Override
    public ChatLanguageModel create(ModelConfig modelConfig, Map<String, Object> parameter) {
        QWenParameter qWenParameter = BeanUtil.toBean(parameter, QWenParameter.class);
        Set<ConstraintViolation<QWenParameter>> validated = ValidationUtil.validate(qWenParameter);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));
        return QwenChatModel.builder()
                .modelName(modelConfig.getModelName())
                .baseUrl(qWenParameter.getBaseUrl())
                .apiKey(qWenParameter.getApiKey())
                .topK(qWenParameter.getTopK())
                .topP(qWenParameter.getTopP())
                .maxTokens(qWenParameter.getMaxTokens())
                .seed(qWenParameter.getSeed())
                .repetitionPenalty(qWenParameter.getRepetitionPenalty())
                .temperature(qWenParameter.getTemperature())
                .stops(qWenParameter.getStop())
                .build();
    }
}
