package io.autoflow.app.rest;

import dev.langchain4j.model.chat.StreamingChatModel;
import io.autoflow.agent.ReActAgent;
import io.autoflow.app.config.ModelRegistry;
import io.autoflow.app.listener.ChatStreamListener;
import io.autoflow.app.model.AgentChatRequest;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.sse.AgentSSEEvent;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * REST controller for agent chat with SSE streaming.
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ReActAgent reActAgent;
    private final ModelRegistry modelRegistry;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;

    public ChatController(ReActAgent reActAgent, ModelRegistry modelRegistry,
                          ChatMessageService chatMessageService, ChatSessionService chatSessionService) {
        this.reActAgent = reActAgent;
        this.modelRegistry = modelRegistry;
        this.chatMessageService = chatMessageService;
        this.chatSessionService = chatSessionService;
    }

    private static final String ERROR_TYPE = "error";

    /**
     * Chat endpoint with SSE streaming.
     *
     * @param request the agent chat request
     * @return SSE emitter for streaming responses
     */
    @PostMapping
    public SseEmitter chat(@Valid @RequestBody AgentChatRequest request) {
        log.info("Chat session started: sessionId={}", request.getSessionId());

        SseEmitter emitter = validateAndCreateEmitter(request);
        if (emitter != null) {
            return emitter;
        }

        String sessionId = request.getSessionId();
        ChatMessage userMessage = createUserMessage(sessionId, request.getInput());
        chatMessageService.save(userMessage);

        SseEmitter streamingEmitter = new SseEmitter(Long.MAX_VALUE);
        StreamingChatModel chatModel = resolveChatModel(request.getModelId());
        runAsyncChat(sessionId, request.getInput(), chatModel, streamingEmitter);

        return streamingEmitter;
    }

    private SseEmitter validateAndCreateEmitter(AgentChatRequest request) {
        String sessionId = request.getSessionId();

        if (sessionId == null || sessionId.isBlank()) {
            log.warn("Chat session rejected: sessionId is required");
            return sendErrorAndComplete("sessionId is required");
        }

        if (request.getInput() == null || request.getInput().isBlank()) {
            log.warn("Chat session rejected: sessionId={}, reason=blank input", sessionId);
            return sendErrorAndComplete("Input cannot be blank");
        }

        return null;
    }

    private ChatMessage createUserMessage(String sessionId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole("USER");
        message.setContent(content);
        return message;
    }

    private StreamingChatModel resolveChatModel(String modelId) {
        StreamingChatModel chatModel = modelRegistry.getModel(modelId);
        log.info("Chat using model: modelId={}, actualModel={}", modelId, chatModel.getClass().getSimpleName());
        return chatModel;
    }

    private void runAsyncChat(String sessionId, String input, StreamingChatModel chatModel, SseEmitter emitter) {
        ChatStreamListener listener = new ChatStreamListener(emitter, sessionId, chatMessageService, chatSessionService);

        CompletableFuture.runAsync(() -> {
            try {
                reActAgent.chat(sessionId, input, chatModel, listener);
            } finally {
                emitter.complete();
                log.info("Chat session ended: sessionId={}", sessionId);
            }
        });
    }

    private SseEmitter sendErrorAndComplete(String errorMessage) {
        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event()
                    .name(ERROR_TYPE)
                    .data(AgentSSEEvent.builder()
                            .type(ERROR_TYPE)
                            .content(errorMessage)
                            .build()));
        } catch (Exception e) {
            log.warn("Failed to send error event", e);
        }
        emitter.complete();
        return emitter;
    }
}
