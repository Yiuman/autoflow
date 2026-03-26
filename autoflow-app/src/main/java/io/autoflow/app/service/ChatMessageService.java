package io.autoflow.app.service;

import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.ChatMessage;
import io.ola.crud.service.CrudService;

import java.util.List;

public interface ChatMessageService extends CrudService<ChatMessage> {

    default List<ChatMessage> findBySessionId(String sessionId) {
        return list(QueryWrapper.create()
                .eq(ChatMessage::getSessionId, sessionId));
    }

    void upsertMessage(String id, String sessionId, String role, String content, String thinkingContent, String status);

    ChatMessage findFirstUserMessage(String sessionId);

    List<ChatMessage> findAiMessagesByConversationId(String conversationId);
}
