package io.autoflow.app.repository;

import io.autoflow.agent.AgentContext;
import io.autoflow.agent.MemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DatabaseMemoryStore implements MemoryStore {
    
    private final Map<String, AgentContext> contexts = new ConcurrentHashMap<>();
    
    @Override
    public AgentContext load(String sessionId) {
        return contexts.computeIfAbsent(sessionId, id -> {
            log.info("Creating new in-memory context for session: {}", id);
            return new AgentContext(id);
        });
    }
    
    @Override
    public void save(AgentContext context) {
        if (context.getSessionId() == null) {
            return;
        }
        contexts.put(context.getSessionId(), context);
        log.info("Saved context for session: {} with {} messages", 
            context.getSessionId(), context.getMessages().size());
    }
}
