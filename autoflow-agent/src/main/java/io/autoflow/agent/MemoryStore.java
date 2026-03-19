package io.autoflow.agent.spi;

/**
 * Memory store for agent context persistence.
 */
public interface MemoryStore {

    /**
     * Load context for a session.
     *
     * @param sessionId the session identifier
     * @return the agent context, or null if not found
     */
    AgentContext load(String sessionId);

    /**
     * Persist context.
     *
     * @param context the agent context to save
     */
    void save(AgentContext context);
}
