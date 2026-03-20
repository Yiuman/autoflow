package io.autoflow.agent.memory;

import io.autoflow.agent.AgentContext;
import io.autoflow.agent.MemoryStore;

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

}
