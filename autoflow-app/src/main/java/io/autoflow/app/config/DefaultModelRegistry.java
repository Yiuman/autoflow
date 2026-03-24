package io.autoflow.app.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of ModelRegistry that maintains a map of modelId to StreamingChatModel.
 */
@Slf4j
public class DefaultModelRegistry implements ModelRegistry {

    private final StreamingChatModel defaultModel;
    private final ChatModel defaultChatModel;
    private final Map<String, StreamingChatModel> models = new ConcurrentHashMap<>();
    private final Map<String, ChatModel> chatModels = new ConcurrentHashMap<>();

    public DefaultModelRegistry(StreamingChatModel defaultModel, ChatModel defaultChatModel) {
        this.defaultModel = defaultModel;
        this.defaultChatModel = defaultChatModel;
    }

    @Override
    public StreamingChatModel getDefaultModel() {
        return defaultModel;
    }

    @Override
    public StreamingChatModel getModel(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return defaultModel;
        }
        return models.getOrDefault(modelId, defaultModel);
    }

    @Override
    public boolean hasModel(String modelId) {
        return modelId != null && !modelId.isBlank() && models.containsKey(modelId);
    }

    @Override
    public ChatModel getDefaultChatModel() {
        return defaultChatModel;
    }

    @Override
    public ChatModel getChatModel(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return defaultChatModel;
        }
        return chatModels.getOrDefault(modelId, defaultChatModel);
    }

    /**
     * Register a chat model with a given ID.
     */
    public void registerModel(String modelId, StreamingChatModel model) {
        models.put(modelId, model);
    }

    /**
     * Register a chat model with a given ID using OpenAI configuration.
     */
    public void registerOpenAiModel(String modelId, String apiKey, String baseUrl, String modelName) {
        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
        registerModel(modelId, streamingModel);

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
        chatModels.put(modelId, chatModel);

        log.info("Registered model: id={}, baseUrl={}, modelName={}", modelId, baseUrl, modelName);
    }
}