package io.autoflow.app.service.impl;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.autoflow.app.config.ModelRegistry;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ChatSessionService;
import io.ola.crud.service.impl.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatSessionServiceImpl extends BaseService<ChatSession> implements ChatSessionService {

    private static final String TITLE_GENERATION_TEMPLATE = """
            Based on this conversation, generate a short title (max 30 Chinese characters).
            Only return the title, nothing else.
            
            User: %s
            Assistant: %s
            
            Title:""";

    private final ModelRegistry modelRegistry;
    private final ChatMessageService chatMessageService;

    public ChatSessionServiceImpl(ModelRegistry modelRegistry, ChatMessageService chatMessageService) {
        this.modelRegistry = modelRegistry;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public void generateTitle(String sessionId) {
        ChatSession session = get(sessionId);
        if (session == null || session.getTitle() != null) {
            return;
        }

        ChatMessage userMsg = chatMessageService.findFirstUserMessage(sessionId);
        if (userMsg == null) {
            return;
        }

        String firstUserMessage = userMsg.getContent();
        String conversationId = userMsg.getConversationId();
        String firstAiResponse = "";

        if (conversationId != null) {
            List<ChatMessage> aiMessages = chatMessageService.findAiMessagesByConversationId(conversationId);
            firstAiResponse = aiMessages.stream()
                    .map(ChatMessage::getContent)
                    .collect(Collectors.joining("\n"));
        }

        String modelId = session.getModelId();
        String title = generateTitleFromLlm(modelId, firstUserMessage, firstAiResponse);
        if (title == null || title.isBlank()) {
            title = getFallbackTitle(firstUserMessage);
        }

        session.setTitle(title);
        save(session);
        log.info("Title generated for session {}: {}", sessionId, title);
    }

    private String generateTitleFromLlm(String modelId, String firstUserMessage, String firstAiResponse) {
        try {
            ChatModel chatModel = modelId != null
                    ? modelRegistry.getChatModel(modelId)
                    : modelRegistry.getDefaultChatModel();
            if (chatModel == null) {
                log.warn("No ChatModel available for title generation, modelId={}", modelId);
                return null;
            }
            String prompt = String.format(TITLE_GENERATION_TEMPLATE, firstUserMessage, firstAiResponse);
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
