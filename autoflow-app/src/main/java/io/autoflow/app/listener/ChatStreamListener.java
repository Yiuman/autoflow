package io.autoflow.app.listener;

import io.autoflow.agent.StreamListener;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.model.sse.AgentSSEEvent;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bridges StreamListener callbacks from ReActAgent to SSE events.
 */
@Slf4j
public class ChatStreamListener implements StreamListener {

    private final SseEmitter sseEmitter;
    private final String sessionId;
    private final String conversationId;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;

    private final StringBuilder thinkingBuffer = new StringBuilder();
    private final StringBuilder contentBuffer = new StringBuilder();
    private final List<ToolCallRecord> toolCalls = new ArrayList<>();

    private ToolCallRecord currentToolCall;

    private static class ToolCallRecord {
        String toolName;
        String arguments;
        StringBuilder resultBuffer = new StringBuilder();
    }

    public ChatStreamListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
        this.sessionId = null;
        this.conversationId = null;
        this.chatMessageService = null;
        this.chatSessionService = null;
    }

    public ChatStreamListener(SseEmitter sseEmitter, String sessionId, String conversationId,
                              ChatMessageService chatMessageService, ChatSessionService chatSessionService) {
        this.sseEmitter = sseEmitter;
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.chatMessageService = chatMessageService;
        this.chatSessionService = chatSessionService;
    }

    @Override
    public void onThinking(String thinking) {
        thinkingBuffer.append(thinking);
        sendEvent("thinking", AgentSSEEvent.builder()
                .type("thinking")
                .content(thinking)
                .build());
    }

    @Override
    public void onToken(String token) {
        contentBuffer.append(token);
        sendEvent("token", AgentSSEEvent.builder()
                .type("token")
                .content(token)
                .build());
    }

    @Override
    public void onToolCallStart(String toolName, String arguments) {
        currentToolCall = new ToolCallRecord();
        currentToolCall.toolName = toolName;
        currentToolCall.arguments = arguments;
        sendEvent("tool_start", AgentSSEEvent.builder()
                .type("tool_start")
                .toolName(toolName)
                .arguments(arguments)
                .build());
    }

    @Override
    public void onToolCallEnd(String toolName, Object result) {
        if (currentToolCall != null) {
            currentToolCall.resultBuffer.append(result != null ? result.toString() : "");
            toolCalls.add(currentToolCall);
            currentToolCall = null;
        }
        sendEvent("tool_end", AgentSSEEvent.builder()
                .type("tool_end")
                .toolName(toolName)
                .result(result)
                .build());
    }

    @Override
    public void onComplete(String fullOutput) {
        try {
            if (thinkingBuffer.isEmpty() && contentBuffer.isEmpty()) {
                log.debug("onComplete: buffers already saved by onRoundComplete, skipping");
            } else {
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSessionId(sessionId);
                aiMsg.setConversationId(conversationId);
                aiMsg.setRole("ASSISTANT");
                aiMsg.setContent(contentBuffer.toString());
                aiMsg.setThinkingContent(thinkingBuffer.toString());
                if (!toolCalls.isEmpty()) {
                    aiMsg.setMetadata(buildToolCallsJson());
                }
                chatMessageService.save(aiMsg);
            }

            ChatSession session = chatSessionService.get(sessionId);
            if (session != null) {
                session.setStatus("COMPLETED");
                chatSessionService.save(session);
            }

            sendEvent("complete", AgentSSEEvent.builder()
                    .type("complete")
                    .content(fullOutput)
                    .build());
        } catch (Exception e) {
            log.error("Failed to save AI message on complete: {}", e.getMessage(), e);
            sendEvent("error", AgentSSEEvent.builder()
                    .type("error")
                    .content("Failed to save message: " + e.getMessage())
                    .build());
        } finally {
            thinkingBuffer.setLength(0);
            contentBuffer.setLength(0);
            toolCalls.clear();
        }
    }

    @Override
    public void onError(Throwable e) {
        try {
            ChatMessage errorMsg = new ChatMessage();
            errorMsg.setSessionId(sessionId);
            errorMsg.setRole("ERROR");
            errorMsg.setContent("Error: " + e.getMessage());
            chatMessageService.save(errorMsg);

            ChatSession session = chatSessionService.get(sessionId);
            if (session != null) {
                session.setStatus("FAILED");
                chatSessionService.save(session);
            }

            sendEvent("error", AgentSSEEvent.builder()
                    .type("error")
                    .content(e.getMessage())
                    .build());
        } catch (Exception ex) {
            log.error("Failed to save error message: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void onRoundComplete() {
        if (thinkingBuffer.isEmpty() && contentBuffer.isEmpty()) {
            return;
        }
        try {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(sessionId);
            aiMsg.setConversationId(conversationId);
            aiMsg.setRole("ASSISTANT");
            aiMsg.setContent(contentBuffer.toString());
            aiMsg.setThinkingContent(thinkingBuffer.toString());
            if (!toolCalls.isEmpty()) {
                aiMsg.setMetadata(buildToolCallsJson());
            }
            chatMessageService.save(aiMsg);
            thinkingBuffer.setLength(0);
            contentBuffer.setLength(0);
            toolCalls.clear();
        } catch (Exception e) {
            log.error("Failed to save AI message on round complete: {}", e.getMessage(), e);
        }
    }

    private void sendEvent(String eventName, AgentSSEEvent event) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(event));
        } catch (IOException e) {
            log.warn("SSE send failed for event {}: {}", eventName, e.getMessage());
        }
    }
    
    private String buildToolCallsJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\"toolCalls\":[");
        for (int i = 0; i < toolCalls.size(); i++) {
            ToolCallRecord tool = toolCalls.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"toolName\":\"").append(escapeJson(tool.toolName)).append("\",");
            json.append("\"arguments\":\"").append(escapeJson(tool.arguments)).append("\",");
            json.append("\"result\":\"").append(escapeJson(tool.resultBuffer.toString())).append("\"");
            json.append("}");
        }
        json.append("]}");
        return json.toString();
    }
    
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
