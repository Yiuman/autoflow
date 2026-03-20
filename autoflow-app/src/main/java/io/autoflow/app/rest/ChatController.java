package io.autoflow.app.rest;

import io.autoflow.agent.ReActAgent;
import io.autoflow.app.listener.ChatStreamListener;
import io.autoflow.app.model.AgentChatRequest;
import io.autoflow.app.model.sse.AgentSSEEvent;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    public ChatController(ReActAgent reActAgent) {
        this.reActAgent = reActAgent;
    }

    /**
     * Chat endpoint with SSE streaming.
     *
     * @param request the agent chat request
     * @return SSE emitter for streaming responses
     */
    @PostMapping("/")
    public SseEmitter chat(@Valid @RequestBody AgentChatRequest request) {
        log.info("Chat session started: sessionId={}", request.getSessionId());

        if (request.getInput() == null || request.getInput().isBlank()) {
            log.warn("Chat session rejected: sessionId={}, reason=blank input", request.getSessionId());
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(AgentSSEEvent.builder()
                                .type("error")
                                .content("Input cannot be blank")
                                .build()));
            } catch (Exception e) {
                log.warn("Failed to send error event", e);
            }
            emitter.complete();
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        ChatStreamListener listener = new ChatStreamListener(emitter);

        // Run agent chat asynchronously to avoid blocking the request thread
        CompletableFuture.runAsync(() -> {
            try {
                reActAgent.chat(request.getSessionId(), request.getInput(), listener);
            } finally {
                emitter.complete();
                log.info("Chat session ended: sessionId={}", request.getSessionId());
            }
        });

        return emitter;
    }
}
