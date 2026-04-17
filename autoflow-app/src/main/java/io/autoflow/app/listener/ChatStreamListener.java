package io.autoflow.app.listener;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.agent.StreamListener;
import io.autoflow.agent.ToolCall;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.model.ToolCallRecord;
import io.autoflow.app.model.sse.AgentSSEEvent;
import io.autoflow.app.model.sse.SSEEventType;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bridges StreamListener callbacks from ReActAgent to SSE events.
 */
@Slf4j
@RequiredArgsConstructor
public class ChatStreamListener implements StreamListener {

    private final SseEmitter sseEmitter;
    private final String sessionId;
    private final String conversationId;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final StringBuilder thinkingBuffer = new StringBuilder();
    private final StringBuilder contentBuffer = new StringBuilder();
    private final List<ToolCallRecord> toolCalls = new ArrayList<>();
    private final Map<String, ToolCallRecord> toolCallsMap = new LinkedHashMap<>();

    @Override
    public void onThinkStart() {
        sendEvent(SSEEventType.THINK_START, AgentSSEEvent.builder()
                .type(SSEEventType.THINK_START.getValue())
                .build());
    }

    @Override
    public void onThinking(String thinking) {
        thinkingBuffer.append(thinking);
        sendEvent(SSEEventType.THINKING, AgentSSEEvent.builder()
                .type(SSEEventType.THINKING.getValue())
                .content(thinking)
                .build());
    }

    @Override
    public void onThinkEnd() {
        sendEvent(SSEEventType.THINK_END, AgentSSEEvent.builder()
                .type(SSEEventType.THINK_END.getValue())
                .build());
    }

    @Override
    public void onToken(String token) {
        contentBuffer.append(token);
        sendEvent(SSEEventType.TOKEN, AgentSSEEvent.builder()
                .type(SSEEventType.TOKEN.getValue())
                .content(token)
                .build());
    }

    @Override
    public void onToolCallStart(String toolId, String toolName, String arguments) {
        ToolCallRecord record = new ToolCallRecord();
        record.setToolId(toolId);
        record.setToolName(toolName);
        record.setArguments(arguments);
        toolCalls.add(record);
        toolCallsMap.put(toolId, record);
        sendEvent(SSEEventType.TOOL_START, AgentSSEEvent.builder()
                .type(SSEEventType.TOOL_START.getValue())
                .toolId(toolId)
                .toolName(toolName)
                .arguments(arguments)
                .build());
    }

    @Override
    public void onToolCallEnd(ToolCall toolCall) {
        String toolId = toolCall.toolId();
        ToolCallRecord record = toolCallsMap.get(toolId);
        if (record != null) {
            record.setResult(toolCall.result());
        }
        sendEvent(SSEEventType.TOOL_END, AgentSSEEvent.builder()
                .type(SSEEventType.TOOL_END.getValue())
                .toolId(toolId)
                .toolName(toolCall.toolName())
                .arguments(toolCall.arguments())
                .result(toolCall.result())
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

            sendEvent(SSEEventType.COMPLETE, AgentSSEEvent.builder()
                    .type(SSEEventType.COMPLETE.getValue())
                    .content(fullOutput)
                    .build());
            ThreadUtil.execute(() -> chatSessionService.generateTitle(sessionId));
        } catch (Exception e) {
            log.error("Failed to save AI message on complete: {}", e.getMessage(), e);
            sendEvent(SSEEventType.ERROR, AgentSSEEvent.builder()
                    .type(SSEEventType.ERROR.getValue())
                    .content("Failed to save message: " + e.getMessage())
                    .build());
        } finally {
            thinkingBuffer.setLength(0);
            contentBuffer.setLength(0);
            toolCalls.clear();
            toolCallsMap.clear();
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

            sendEvent(SSEEventType.ERROR, AgentSSEEvent.builder()
                    .type(SSEEventType.ERROR.getValue())
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
            toolCallsMap.clear();
        } catch (Exception e) {
            log.error("Failed to save AI message on round complete: {}", e.getMessage(), e);
        }
    }

    private void sendEvent(SSEEventType eventType, AgentSSEEvent event) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name(eventType.getValue())
                    .data(event));
        } catch (IOException e) {
            log.warn("SSE send failed for event {}: {}", eventType, e.getMessage());
        }
    }

    private String buildToolCallsJson() {
        Map<String, Object> wrapper = Map.of("toolCalls", toolCalls);
        return JSONUtil.toJsonStr(wrapper);
    }
}
