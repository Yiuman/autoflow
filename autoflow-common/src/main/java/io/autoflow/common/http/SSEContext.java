package io.autoflow.common.http;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiuman
 * @date 2024/3/29
 */
public final class SSEContext {
    private static final Map<String, SseEmitter> SSE_EMITTER_MAP = new ConcurrentHashMap<>();

    private SSEContext() {
    }

    public static void add(String flowId, SseEmitter sseEmitter) {
        SSE_EMITTER_MAP.put(flowId, sseEmitter);
    }

    public static SseEmitter get(String flowId) {
        if (Objects.isNull(flowId)) {
            return null;
        }
        return SSE_EMITTER_MAP.get(flowId);
    }

    public static void close(String flowId) {
        SseEmitter sseEmitter = get(flowId);
        if (Objects.nonNull(sseEmitter)) {
            sseEmitter.complete();
            SSE_EMITTER_MAP.remove(flowId);
        }

    }
}
