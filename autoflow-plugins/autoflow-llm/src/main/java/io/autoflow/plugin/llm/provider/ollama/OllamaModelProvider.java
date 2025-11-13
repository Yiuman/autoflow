package io.autoflow.plugin.llm.provider.ollama;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.validation.ValidationUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
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
public class OllamaModelProvider implements ChatLanguageModelProvider {

    @Override
    public ChatModel create(ModelConfig modelConfig, Map<String, Object> parameter) {
        OllamaParameter ollamaParameter = BeanUtil.toBean(parameter, OllamaParameter.class);
        Set<ConstraintViolation<OllamaParameter>> validated = ValidationUtil.validate(ollamaParameter);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));
        String modelName = StrUtil.blankToDefault(ollamaParameter.getModelName(), modelConfig.getModelName());
        return OllamaChatModel.builder()
                .modelName(modelName)
                .baseUrl(ollamaParameter.getBaseUrl())
                .seed(ollamaParameter.getSeed())
                .numCtx(ollamaParameter.getNumCtx())
                .numPredict(ollamaParameter.getNumPredict())
                .topK(ollamaParameter.getTopK())
                .topP(ollamaParameter.getTopP())
                .temperature(ollamaParameter.getTemperature())
                .repeatPenalty(ollamaParameter.getRepeatPenalty())
                .build();

    }
}
