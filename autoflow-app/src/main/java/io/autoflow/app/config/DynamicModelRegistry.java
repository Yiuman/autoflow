package io.autoflow.app.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.autoflow.app.model.Model;
import io.autoflow.app.service.ModelService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DynamicModelRegistry implements ModelRegistry {

    private final Map<String, StreamingChatModel> models = new ConcurrentHashMap<>();
    private final Map<String, ChatModel> chatModels = new ConcurrentHashMap<>();
    private volatile StreamingChatModel defaultModel;
    private volatile ChatModel defaultChatModel;
    private volatile boolean initialized = false;
    private final Object lock = new Object();

    private ModelService modelService;

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
        log.info("DynamicModelRegistry: modelService injected, class={}", modelService.getClass().getName());
    }

    public void setDefaultModel(StreamingChatModel defaultModel) {
        this.defaultModel = defaultModel;
    }

    public void setDefaultChatModel(ChatModel defaultChatModel) {
        this.defaultChatModel = defaultChatModel;
    }

    private void ensureInitialized() {
        if (!initialized || models.isEmpty()) {
            synchronized (lock) {
                if (!initialized || models.isEmpty()) {
                    log.info("DynamicModelRegistry: initializing... (initialized={}, models={})", initialized, models.size());
                    try {
                        loadModelsFromDatabase();
                        initialized = true;
                        log.info("DynamicModelRegistry: initialized with {} models", models.size());
                    } catch (Exception e) {
                        log.error("DynamicModelRegistry: failed to initialize", e);
                    }
                }
            }
        }
    }

    private void loadModelsFromDatabase() {
        if (modelService == null) {
            log.warn("DynamicModelRegistry: modelService is null, cannot load models");
            return;
        }
        try {
            List<Model> modelList = modelService.list();
            log.info("DynamicModelRegistry: found {} models in database", modelList.size());
            for (Model model : modelList) {
                try {
                    StreamingChatModel streamingChatModel = createChatModel(model);
                    models.put(model.getId(), streamingChatModel);
                    ChatModel chatModel = createNonStreamingChatModel(model);
                    chatModels.put(model.getId(), chatModel);
                    log.info("Loaded model: id={}, name={}", model.getId(), model.getName());
                } catch (Exception e) {
                    log.warn("Failed to load model: id={}, error={}", model.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load models from database: {}", e.getMessage(), e);
        }
    }

    private StreamingChatModel createChatModel(Model model) {
        Map<String, Object> config = new HashMap<>();
        if (StrUtil.isNotBlank(model.getConfig())) {
            config = JSONUtil.parseObj(model.getConfig());
        }

        String baseUrl = model.getBaseUrl();
        String apiKey = model.getApiKey();
        String modelName = model.getName();
        Integer timeout = (Integer) config.getOrDefault("timeout", 60);
        Double temperature = (Double) config.getOrDefault("temperature", 0.7);

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .returnThinking(true)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeout))
                .temperature(temperature)
                .build();
    }

    private ChatModel createNonStreamingChatModel(Model model) {
        Map<String, Object> config = new HashMap<>();
        if (StrUtil.isNotBlank(model.getConfig())) {
            config = JSONUtil.parseObj(model.getConfig());
        }

        String baseUrl = model.getBaseUrl();
        String apiKey = model.getApiKey();
        String modelName = model.getName();
        Integer timeout = (Integer) config.getOrDefault("timeout", 60);
        Double temperature = (Double) config.getOrDefault("temperature", 0.7);

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeout))
                .temperature(temperature)
                .build();
    }

    @Override
    public StreamingChatModel getDefaultModel() {
        return defaultModel;
    }

    @Override
    public StreamingChatModel getModel(String modelId) {
        try {
            ensureInitialized();
        } catch (Exception e) {
            log.error("Failed to initialize model registry, using default", e);
        }
        log.info("DynamicModelRegistry.getModel: requested={}, availableModels={}, defaultModel={}",
                modelId, models.keySet(), defaultModel != null ? defaultModel.getClass().getSimpleName() : null);
        if (modelId == null || modelId.isBlank()) {
            return defaultModel;
        }
        StreamingChatModel model = models.get(modelId);
        if (model == null) {
            log.warn("Model not found: {}, using default", modelId);
            return defaultModel;
        }
        return model;
    }

    @Override
    public boolean hasModel(String modelId) {
        ensureInitialized();
        return modelId != null && !modelId.isBlank() && models.containsKey(modelId);
    }

    @Override
    public ChatModel getDefaultChatModel() {
        return defaultChatModel;
    }

    @Override
    public ChatModel getChatModel(String modelId) {
        try {
            ensureInitialized();
        } catch (Exception e) {
            log.error("Failed to initialize model registry, using default", e);
        }
        if (modelId == null || modelId.isBlank()) {
            return defaultChatModel;
        }
        ChatModel model = chatModels.get(modelId);
        if (model == null) {
            log.warn("ChatModel not found: {}, using default", modelId);
            return defaultChatModel;
        }
        return model;
    }

    public void refreshModels() {
        synchronized (lock) {
            models.clear();
            initialized = false;
        }
        ensureInitialized();
    }
}
