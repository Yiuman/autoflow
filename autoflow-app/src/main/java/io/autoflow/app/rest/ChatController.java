package io.autoflow.app.rest;

import io.autoflow.app.dto.ChatRequest;
import io.autoflow.app.dto.SendMessageRequest;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.service.ChatService;
import io.ola.common.http.R;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * REST controller for chat operations.
 * Provides endpoints for session management and message handling with SSE support.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Creates a new chat session.
     *
     * @param request the request containing optional title
     * @return the created session
     */
    @PostMapping("/sessions")
    public R<ChatSession> createSession(@RequestBody(required = false) final Map<String, String> request) {
        String title = null;
        if (request != null) {
            title = request.get("title");
        }
        return R.ok(chatService.createSession(title));
    }

    /**
     * Gets all chat sessions.
     *
     * @return list of all sessions
     */
    @GetMapping("/sessions")
    public R<List<ChatSession>> getSessions() {
        return R.ok(chatService.getSessions());
    }

    /**
     * Gets a specific session by ID.
     *
     * @param id the session ID
     * @return the session
     */
    @GetMapping("/sessions/{id}")
    public R<ChatSession> getSession(@PathVariable final String id) {
        return R.ok(chatService.getSession(id));
    }

    /**
     * Deletes a chat session.
     *
     * @param id the session ID
     * @return void
     */
    @DeleteMapping("/sessions/{id}")
    public R<Void> deleteSession(@PathVariable final String id) {
        chatService.deleteSession(id);
        return R.ok();
    }

    /**
     * Gets all messages for a specific session.
     *
     * @param id the session ID
     * @return list of messages in the session
     */
    @GetMapping("/sessions/{id}/messages")
    public R<List<ChatMessage>> getSessionMessages(@PathVariable final String id) {
        return R.ok(chatService.getMessages(id));
    }

    /**
     * Sends a message in a session and returns SSE stream.
     * This endpoint returns a Server-Sent Events stream that emits
     * message events, tool call events, and completion/error events.
     *
     * @param id      the session ID
     * @param request the message request containing content and optional provider
     * @return SSE emitter for streaming events
     */
    @PostMapping(value = "/sessions/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(
            @PathVariable final String id,
            @RequestBody final SendMessageRequest request) {
        return chatService.sendMessageStreaming(id, request.getContent(), request.getProvider());
    }

    /**
     * Sends a message in a session (synchronous version, returns the response directly).
     *
     * @param id      the session ID
     * @param request the chat request
     * @return the assistant's response message
     */
    @PostMapping("/sessions/{id}/messages/sync")
    public R<ChatMessage> sendMessageSync(
            @PathVariable final String id,
            @RequestBody final ChatRequest request) {
        return R.ok(chatService.sendMessage(id, request.getMessage(), request.getProvider()));
    }

    /**
     * Executes a tool directly and returns the result.
     *
     * @param request the tool execution request containing tool name and parameters
     * @return the tool call record with result
     */
    @PostMapping("/tools/execute")
    public R<io.autoflow.app.model.ToolCall> executeTool(@RequestBody final ToolExecutionRequest request) {
        return R.ok(chatService.executeTool(request.getToolName(), request.getParameters()));
    }

    /**
     * Request object for tool execution.
     */
    @lombok.Data
    public static class ToolExecutionRequest {
        private String toolName;
        private Map<String, Object> parameters;
    }
}
