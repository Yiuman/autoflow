package io.autoflow.app.repository;

import io.autoflow.agent.AgentContext;
import io.autoflow.agent.MemoryStore;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.spi.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DatabaseMemoryStore implements MemoryStore {
    
    private final Map<String, AgentContext> contexts = new ConcurrentHashMap<>();
    private final ChatMessageService chatMessageService;
    
    public DatabaseMemoryStore(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }
    
    @Override
    public AgentContext load(String sessionId) {
        AgentContext context = contexts.computeIfAbsent(sessionId, id -> {
            log.info("Creating new in-memory context for session: {}", id);
            return new AgentContext(id);
        });
        
        if (context.getMessages().isEmpty()) {
            loadHistoryFromDatabase(sessionId, context);
        }
        
        return context;
    }
    
    private void loadHistoryFromDatabase(String sessionId, AgentContext context) {
        List<io.autoflow.app.model.ChatMessage> dbMessages = chatMessageService.findBySessionId(sessionId);
        log.info("Loaded {} messages from database for session: {}", dbMessages.size(), sessionId);
        
        for (io.autoflow.app.model.ChatMessage dbMsg : dbMessages) {
            String role = dbMsg.getRole();
            
            if ("ERROR".equals(role)) {
                continue;
            }
            
            io.autoflow.spi.model.ChatMessage spiMsg = new io.autoflow.spi.model.ChatMessage();
            spiMsg.setContent(dbMsg.getContent());
            
            if ("USER".equals(role)) {
                spiMsg.setType(MessageType.USER);
                context.getMessages().add(spiMsg);
            } else if ("ASSISTANT".equals(role)) {
                spiMsg.setType(MessageType.ASSISTANT);
                context.getMessages().add(spiMsg);
            }
        }
        
        log.info("Populated context with {} messages (skipping ERRORs)", context.getMessages().size());
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
