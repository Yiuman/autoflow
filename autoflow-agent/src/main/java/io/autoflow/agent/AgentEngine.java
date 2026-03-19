package io.autoflow.agent;

/**
 * Agent engine interface for handling chat conversations.
 */
public interface AgentEngine {

    /**
     * Send a chat message to the agent.
     *
     * @param sessionId the session identifier
     * @param input     the user input message
     * @param listener  callback for streaming events
     */
    void chat(String sessionId, String input, StreamListener listener);
}
