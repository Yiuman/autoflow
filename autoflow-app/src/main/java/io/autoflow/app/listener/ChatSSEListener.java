package io.autoflow.app.listener;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.dto.event.ChatEvent;
import io.autoflow.app.dto.event.ErrorEvent;
import io.autoflow.app.dto.event.MessageEvent;
import io.autoflow.app.dto.event.ToolCallEvent;
import io.autoflow.common.http.SSEContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Listener for chat events that emits them to SSE clients.
 * This listener handles MessageEvent, ToolCallEvent, and ErrorEvent
 * and forwards them to connected SSE clients.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Component
@Slf4j
public class ChatSSEListener {

    /**
     * Handles a chat event by sending it to the appropriate SSE client.
     *
     * @param event the chat event to handle
     */
    public void onChatEvent(final ChatEvent event) {
        if (Objects.isNull(event)) {
            return;
        }

        String sessionId = event.getSessionId();
        SseEmitter sseEmitter = SSEContext.get(sessionId);
        if (Objects.isNull(sseEmitter)) {
            log.debug("No SSE connection found for session: {}", sessionId);
            return;
        }

        try {
            SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
                    .name(event.getType())
                    .data(JSONUtil.toJsonStr(event));

            if (event instanceof MessageEvent) {
                handleMessageEvent((MessageEvent) event, eventBuilder);
            } else if (event instanceof ToolCallEvent) {
                handleToolCallEvent((ToolCallEvent) event, eventBuilder);
            } else if (event instanceof ErrorEvent) {
                handleErrorEvent((ErrorEvent) event, eventBuilder);
            }

            sseEmitter.send(eventBuilder);
        } catch (Throwable throwable) {
            log.warn("Failed to send chat event to SSE client for session: {}", sessionId, throwable);
        }
    }

    /**
     * Registers this listener as a consumer of chat events.
     * Can be used with event buses or other event distribution mechanisms.
     *
     * @return a consumer that processes chat events
     */
    public Consumer<ChatEvent> asConsumer() {
        return this::onChatEvent;
    }

    private void handleMessageEvent(final MessageEvent event, final SseEmitter.SseEventBuilder builder) {
        builder.id(Objects.nonNull(event.getMessageId()) ? event.getMessageId() : event.getTimestamp().toString());
        log.debug("Sending message event for session {}: role={}", event.getSessionId(), event.getRole());
    }

    private void handleToolCallEvent(final ToolCallEvent event, final SseEmitter.SseEventBuilder builder) {
        builder.id(Objects.nonNull(event.getToolCallId()) ? event.getToolCallId() : event.getTimestamp().toString());
        log.debug("Sending tool call event for session {}: tool={}, status={}",
                event.getSessionId(), event.getToolName(), event.getStatus());
    }

    private void handleErrorEvent(final ErrorEvent event, final SseEmitter.SseEventBuilder builder) {
        log.debug("Sending error event for session {}: code={}, message={}",
                event.getSessionId(), event.getErrorCode(), event.getMessage());
    }
}
