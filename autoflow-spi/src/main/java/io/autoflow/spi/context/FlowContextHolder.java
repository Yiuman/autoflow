package io.autoflow.spi.context;

import cn.hutool.core.thread.threadlocal.NamedThreadLocal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FlowContextHolder {
    private static final Map<String, ExecutionContext> ID_CONTEXT_MAP = new HashMap<>();
    private static final ThreadLocal<ExecutionContext> CONTEXT_THREAD_LOCAL
            = new NamedThreadLocal<>(ExecutionContext.class.getName());

    private static final ThreadLocal<String> ID_THREAD_LOCAL
            = new NamedThreadLocal<>(ExecutionContext.class.getName());

    public static ExecutionContext get() {
        ExecutionContext flowExecutionContext = Optional.ofNullable(CONTEXT_THREAD_LOCAL.get())
                .orElse(new FlowExecutionContextImpl());
        CONTEXT_THREAD_LOCAL.set(flowExecutionContext);
        return flowExecutionContext;
    }

    public static ExecutionContext get(String id, Map<String, Object> data) {
        FlowExecutionContextImpl flowExecutionContext = FlowExecutionContextImpl.create(
                Optional.ofNullable(data).orElseGet(HashMap::new)
        );
        CONTEXT_THREAD_LOCAL.set(flowExecutionContext);
        ID_THREAD_LOCAL.set(id);
        ID_CONTEXT_MAP.put(id, flowExecutionContext);
        return flowExecutionContext;
    }

    public static void remove() {
        String id = ID_THREAD_LOCAL.get();
        ID_CONTEXT_MAP.remove(id);
        ID_THREAD_LOCAL.remove();
        CONTEXT_THREAD_LOCAL.remove();
    }

    public static void interrupt(String id) {
        ExecutionContext executionContext = ID_CONTEXT_MAP.get(id);
        if (Objects.nonNull(executionContext) && executionContext instanceof FlowExecutionContext flowExecutionContext) {
            flowExecutionContext.interrupt();
        }
    }

}
