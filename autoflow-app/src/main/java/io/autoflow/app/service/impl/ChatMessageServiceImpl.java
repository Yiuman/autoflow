package io.autoflow.app.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.service.ChatMessageService;
import io.ola.crud.service.impl.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageServiceImpl extends BaseService<ChatMessage> implements ChatMessageService {
    
    public List<ChatMessage> findBySessionId(String sessionId) {
        return list(QueryWrapper.create()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderBy("create_time", true));
    }

    @Override
    public void upsertMessage(String id, String sessionId, String role, String content, String thinkingContent, String status) {
        ChatMessage existing = get(id);
        if (existing != null) {
            existing.setContent(content);
            existing.setThinkingContent(thinkingContent);
            save(existing);
        } else {
            ChatMessage newMsg = new ChatMessage();
            newMsg.setId(id);
            newMsg.setSessionId(sessionId);
            newMsg.setRole(role);
            newMsg.setContent(content);
            newMsg.setThinkingContent(thinkingContent);
            save(newMsg);
        }
    }

    @Override
    public ChatMessage findFirstUserMessage(String sessionId) {
        List<ChatMessage> messages = list(QueryWrapper.create()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getRole, "USER")
                .orderBy("create_time", true)
                .limit(1));
        return messages.isEmpty() ? null : messages.get(0);
    }

    @Override
    public List<ChatMessage> findAiMessagesByConversationId(String conversationId) {
        return list(QueryWrapper.create()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getRole, "ASSISTANT")
                .orderBy("create_time", true));
    }
}
