package io.autoflow.app.service.impl;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.autoflow.app.config.ModelRegistry;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.service.ChatSessionService;
import io.ola.crud.service.impl.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatSessionServiceImpl extends BaseService<ChatSession> implements ChatSessionService {

    private final ModelRegistry modelRegistry;

    public ChatSessionServiceImpl(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    @Override
    public void generateTitle(String sessionId, String firstUserMessage) {
        String title = generateTitleFromLlm(firstUserMessage);
        if (title == null || title.isBlank()) {
            title = getFallbackTitle(firstUserMessage);
        }
        ChatSession session = getById(sessionId);
        if (session != null) {
            session.setTitle(title);
            update(session);
        }
    }

    private String generateTitleFromLlm(String firstUserMessage) {
        try {
            ChatModel chatModel = modelRegistry.getDefaultChatModel();
            if (chatModel == null) {
                log.warn("No ChatModel available for title generation");
                return null;
            }
            String prompt = "Given this user message, generate a short title (max 30 Chinese characters) for this chat session. Only return the title, nothing else. Message: '" + firstUserMessage + "' Title:";
            ChatResponse response = chatModel.chat(UserMessage.from(prompt));
            String title = response.aiMessage().text();
            if (title != null) {
                title = title.trim();
            }
            return title;
        } catch (Exception e) {
            log.error("Failed to generate title from LLM: {}", e.getMessage());
            return null;
        }
    }

    private String getFallbackTitle(String firstUserMessage) {
        if (firstUserMessage == null || firstUserMessage.isBlank()) {
            return "新对话";
        }
        if (firstUserMessage.length() <= 20) {
            return firstUserMessage;
        }
        return firstUserMessage.substring(0, 20) + "...";
    }
}
