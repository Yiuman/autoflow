package io.autoflow.app.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * Registry for looking up StreamingChatModel and ChatModel instances by model ID.
 */
public interface ModelRegistry {
    
    /**
     * Get the default streaming chat model when no modelId is specified.
     */
    StreamingChatModel getDefaultModel();
    
    /**
     * Get a streaming chat model by its ID.
     * 
     * @param modelId the model identifier
     * @return the corresponding chat model, or null if not found
     */
    StreamingChatModel getModel(String modelId);
    
    /**
     * Check if a model with the given ID exists.
     */
    boolean hasModel(String modelId);
    
    /**
     * Get the default non-streaming chat model for title generation and other single-turn tasks.
     */
    ChatModel getDefaultChatModel();
    
    /**
     * Get a non-streaming chat model by its ID.
     * 
     * @param modelId the model identifier
     * @return the corresponding chat model, or null if not found
     */
    ChatModel getChatModel(String modelId);
}