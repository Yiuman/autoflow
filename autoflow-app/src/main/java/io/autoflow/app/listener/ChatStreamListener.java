package io.autoflow.app.listener;

import io.autoflow.agent.StreamListener;
import io.autoflow.app.model.sse.AgentSSEEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * Bridges StreamListener callbacks from ReActAgent to SSE events.
 */
@Slf4j
public class ChatStreamListener implements StreamListener {

    private final SseEmitter sseEmitter;

    public ChatStreamListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void onThinking(String thinking) {
        sendEvent("thinking", AgentSSEEvent.builder()
                .type("thinking")
                .content(thinking)
                .build());
    }

    @Override
    public void onToken(String token) {
        sendEvent("token", AgentSSEEvent.builder()
                .type("token")
                .content(token)
                .build());
    }

    @Override
    public void onToolCallStart(String toolName, String arguments) {
        sendEvent("tool_start", AgentSSEEvent.builder()
                .type("tool_start")
                .toolName(toolName)
                .arguments(arguments)
                .build());
    }

    @Override
    public void onToolCallEnd(String toolName, Object result) {
        sendEvent("tool_end", AgentSSEEvent.builder()
                .type("tool_end")
                .toolName(toolName)
                .result(result)
                .build());
    }

    @Override
    public void onComplete(String fullOutput) {
        sendEvent("complete", AgentSSEEvent.builder()
                .type("complete")
                .content(fullOutput)
                .build());
    }

    @Override
    public void onError(Throwable e) {
        sendEvent("error", AgentSSEEvent.builder()
                .type("error")
                .content(e.getMessage())
                .build());
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
}
