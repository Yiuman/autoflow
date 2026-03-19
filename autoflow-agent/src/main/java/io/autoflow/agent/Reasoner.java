package io.autoflow.agent;

/**
 * Interface for LLM streaming inference.
 * Implementations handle token streaming via the listener.
 */
public interface Reasoner {

    void think(AgentContext context, StreamListener listener);

    void think(String systemPrompt, AgentContext context, StreamListener listener);
}
