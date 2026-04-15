package io.autoflow.app.rest;

import io.autoflow.app.model.AgentChatRequest;
import io.autoflow.app.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller for agent chat with SSE streaming.
 * Delegates all business logic to ChatService.
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Chat endpoint with SSE streaming.
     *
     * @param request the agent chat request
     * @return SSE emitter for streaming responses
     */
    @PostMapping
    public SseEmitter chat(@Valid @RequestBody AgentChatRequest request) {
        return chatService.chat(request);
    }
}
