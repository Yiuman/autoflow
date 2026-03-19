package io.autoflow.agent.core.memory;

import io.autoflow.agent.spi.AgentContext;
import io.autoflow.agent.spi.MemoryStore;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of MemoryStore using ConcurrentHashMap.
 */
public class InMemoryMemoryStore implements MemoryStore {

    private final ConcurrentHashMap<String, AgentContext> store = new ConcurrentHashMap<>();

    @Override
    public AgentContext load(String sessionId) {
        return store.computeIfAbsent(sessionId, AgentContext::new);
    }

    @Override
    public void save(AgentContext context) {
        store.put(context.getSessionId(), context);
    }

    /**
     * Factory method to create a new AgentContext.
     *
     * @param sessionId the session identifier
     * @return a new AgentContext instance
     */
    public static AgentContext createContext(String sessionId) {
        return new AgentContext(sessionId);
    }
}
