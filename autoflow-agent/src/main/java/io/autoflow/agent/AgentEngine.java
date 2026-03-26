package io.autoflow.agent;

/**
 * SPI interface for agent implementations.
 */
public interface AgentEngine {

    /**
     * Run a chat turn with the agent.
     *
     * @param request  the chat request containing input and history
     * @param listener callback for streaming events
     */
    void chat(ChatRequest request, StreamListener listener);
}
