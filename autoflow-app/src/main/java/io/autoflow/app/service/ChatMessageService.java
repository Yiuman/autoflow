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

    default ChatMessage findFirstUserMessage(String sessionId) {
        return get(
                QueryWrapper.create()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .eq(ChatMessage::getRole, "USER")
                        .orderBy(ChatMessage::getCreateTime)
                        .asc()
        );

    }

    default List<ChatMessage> findAiMessagesByConversationId(String conversationId) {
        return list(
                QueryWrapper.create()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .eq(ChatMessage::getRole, "ASSISTANT")
                        .orderBy(ChatMessage::getCreateTime)
                        .asc()
        );
    }

}
