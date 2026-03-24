# Task 2: Backend Chat SSE Support modelId

## Summary
Added `modelId` support to the chat SSE endpoint for per-request model selection.

## Changes Made

### 1. AgentChatRequest.java
Added `modelId` field (String, optional):
```java
private String modelId;
```

**File**: `autoflow-app/src/main/java/io/autoflow/app/model/AgentChatRequest.java`

### 2. ModelRegistry Interface
Created new interface for model lookup:
```java
public interface ModelRegistry {
    StreamingChatModel getDefaultModel();
    StreamingChatModel getModel(String modelId);
    boolean hasModel(String modelId);
}
```

**File**: `autoflow-app/src/main/java/io/autoflow/app/config/ModelRegistry.java`

### 3. DefaultModelRegistry Implementation
Created implementation that maintains a map of modelId to StreamingChatModel:
```java
public class DefaultModelRegistry implements ModelRegistry {
    private final StreamingChatModel defaultModel;
    private final Map<String, StreamingChatModel> models = new ConcurrentHashMap<>();
    
    public StreamingChatModel getModel(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return defaultModel;
        }
        return models.getOrDefault(modelId, defaultModel);
    }
}
```

**File**: `autoflow-app/src/main/java/io/autoflow/app/config/DefaultModelRegistry.java`

### 4. ReActAgent.java
Added overloaded `chat()` method that accepts a `StreamingChatModel` directly:
```java
public void chat(String sessionId, String input, StreamingChatModel model, StreamListener listener) {
    // Uses provided model instead of internal chatModel
}

@Override
public void chat(String sessionId, String input, StreamListener listener) {
    chat(sessionId, input, chatModel, listener); // Delegates to overloaded method
}
```

**File**: `autoflow-agent/src/main/java/io/autoflow/agent/ReActAgent.java`

### 5. ChatController.java
Updated to inject `ModelRegistry` and select model based on `modelId`:
```java
public ChatController(ReActAgent reActAgent, ModelRegistry modelRegistry) {
    this.reActAgent = reActAgent;
    this.modelRegistry = modelRegistry;
}

// In chat() method:
String modelId = request.getModelId();
StreamingChatModel chatModel = modelRegistry.getModel(modelId);
reActAgent.chat(request.getSessionId(), request.getInput(), chatModel, listener);
```

**File**: `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java`

### 6. BeanConfig.java
Added `ModelRegistry` bean creation:
```java
@Bean
public ModelRegistry modelRegistry(OpenAiStreamingChatModel defaultModel) {
    DefaultModelRegistry registry = new DefaultModelRegistry(defaultModel);
    
    // Register additional models via environment variables
    String altApiKey = System.getenv("OPENAI_API_KEY_2");
    String altBaseUrl = System.getenv("OPENAI_BASE_URL_2");
    String altModelName = System.getenv("OPENAI_MODEL_NAME_2");
    if (altApiKey != null && altBaseUrl != null && altModelName != null) {
        registry.registerOpenAiModel("model2", altApiKey, altBaseUrl, altModelName);
    }
    
    return registry;
}
```

**File**: `autoflow-app/src/main/java/io/autoflow/app/config/BeanConfig.java`

## Behavior

- When `modelId` is `null` or blank → uses default model
- When `modelId` is provided but not registered → falls back to default model
- When `modelId` matches a registered model → uses that model

## Acceptance Criteria

- [x] AgentChatRequest accepts modelId parameter
- [x] ChatController passes modelId to ReActAgent (via ModelRegistry lookup)
- [x] ReActAgent uses modelId to select appropriate chat model (via passed StreamingChatModel)
- [x] Default model used when modelId is null

## SSE Response Format
Unchanged - maintains backward compatibility.
