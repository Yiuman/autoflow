package io.autoflow.app.llm;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.ModelParameterProvider;
import io.autoflow.plugin.llm.provider.ChatLanguageModelProvider;
import io.autoflow.plugin.llm.provider.ChatModelProviders;

import java.util.Map;
import java.util.Objects;

/**
 * Factory for creating ChatModel instances using existing LLM providers.
 * This factory reuses the existing ChatModelProviders infrastructure to support
 * multiple LLM providers (OpenAI, Gemini, Ollama, Qwen, etc.).
 *
 * @author autoflow
 * @date 2025/03/04
 */
public final class ChatModelFactory {

    private ChatModelFactory() {
    }

    /**
     * Creates a ChatModel instance based on the provider name and configuration.
     *
     * @param providerName the name of the provider (e.g., "openai", "gemini", "ollama")
     * @param config       the configuration map containing provider-specific parameters
     * @return the ChatModel instance
     * @throws IllegalArgumentException if the provider is not found or configuration is invalid
     */
    public static ChatModel getModel(final String providerName, final Map<String, Object> config) {
        ModelConfig modelConfig = resolveModelConfig(providerName, config);
        ChatLanguageModelProvider provider = getProvider(modelConfig);
        return provider.create(modelConfig, config);
    }

    /**
     * Creates a StreamingChatModel instance based on the provider name and configuration.
     *
     * @param providerName the name of the provider (e.g., "openai", "gemini", "ollama")
     * @param config       the configuration map containing provider-specific parameters
     * @return the StreamingChatModel instance
     * @throws IllegalArgumentException if the provider is not found or configuration is invalid
     */
    public static StreamingChatModel getStreamingModel(
            final String providerName, final Map<String, Object> config) {
        ModelConfig modelConfig = resolveModelConfig(providerName, config);
        ChatLanguageModelProvider provider = getProvider(modelConfig);
        return provider.createStream(modelConfig, config);
    }

    /**
     * Creates a ChatModel instance from a model configuration name.
     *
     * @param modelName the model name as defined in the model configuration
     * @param config    the configuration map containing provider-specific parameters
     * @return the ChatModel instance
     * @throws IllegalArgumentException if the model configuration is not found
     */
    public static ChatModel getModelByModelName(final String modelName, final Map<String, Object> config) {
        ModelConfig modelConfig = ModelParameterProvider.getModelConfigByModelName(modelName);
        if (Objects.isNull(modelConfig)) {
            throw new IllegalArgumentException("Model configuration not found for: " + modelName);
        }
        ChatLanguageModelProvider provider = getProvider(modelConfig);
        return provider.create(modelConfig, config);
    }

    /**
     * Creates a StreamingChatModel instance from a model configuration name.
     *
     * @param modelName the model name as defined in the model configuration
     * @param config    the configuration map containing provider-specific parameters
     * @return the StreamingChatModel instance
     * @throws IllegalArgumentException if the model configuration is not found
     */
    public static StreamingChatModel getStreamingModelByModelName(
            final String modelName, final Map<String, Object> config) {
        ModelConfig modelConfig = ModelParameterProvider.getModelConfigByModelName(modelName);
        if (Objects.isNull(modelConfig)) {
            throw new IllegalArgumentException("Model configuration not found for: " + modelName);
        }
        ChatLanguageModelProvider provider = getProvider(modelConfig);
        return provider.createStream(modelConfig, config);
    }

    /**
     * Resolves the model configuration based on provider name and config.
     *
     * @param providerName the provider name
     * @param config       the configuration map
     * @return the resolved ModelConfig
     * @throws IllegalArgumentException if the model configuration cannot be resolved
     */
    private static ModelConfig resolveModelConfig(
            final String providerName, final Map<String, Object> config) {
        String modelName = extractModelName(config);
        if (StrUtil.isNotBlank(modelName)) {
            ModelConfig modelConfig = ModelParameterProvider.getModelConfigByModelName(modelName);
            if (Objects.nonNull(modelConfig)) {
                return modelConfig;
            }
        }

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setProvider(providerName);
        modelConfig.setModelName(modelName);
        if (Objects.nonNull(config)) {
            Object implClass = config.get("implClass");
            if (Objects.nonNull(implClass)) {
                modelConfig.setImplClass(implClass.toString());
            }
        }
        return modelConfig;
    }

    /**
     * Extracts the model name from the configuration.
     *
     * @param config the configuration map
     * @return the model name, or null if not found
     */
    private static String extractModelName(final Map<String, Object> config) {
        if (Objects.isNull(config)) {
            return null;
        }
        Object modelName = config.get("modelName");
        if (Objects.nonNull(modelName)) {
            return modelName.toString();
        }
        modelName = config.get("model");
        if (Objects.nonNull(modelName)) {
            return modelName.toString();
        }
        return null;
    }

    /**
     * Gets the ChatLanguageModelProvider for the given model configuration.
     *
     * @param modelConfig the model configuration
     * @return the ChatLanguageModelProvider
     * @throws IllegalArgumentException if the provider implementation class is not set
     */
    private static ChatLanguageModelProvider getProvider(final ModelConfig modelConfig) {
        String implClass = modelConfig.getImplClass();
        if (StrUtil.isBlank(implClass)) {
            throw new IllegalArgumentException(
                    "Provider implementation class not configured for model: " + modelConfig.getModelName());
        }
        return ChatModelProviders.get(implClass);
    }
}
