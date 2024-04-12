package io.autoflow.spi.context;

import cn.hutool.core.thread.threadlocal.NamedThreadLocal;

import java.util.Optional;

public final class FlowContextHolder {
    private static final ThreadLocal<ExecutionContext> CONTEXT_THREAD_LOCAL
            = new NamedThreadLocal<>(ExecutionContext.class.getName());

    public static ExecutionContext get() {
        ExecutionContext flowExecutionContext = Optional.ofNullable(CONTEXT_THREAD_LOCAL.get())
                .orElse(new FlowExecutionContext());
        CONTEXT_THREAD_LOCAL.set(flowExecutionContext);
        return flowExecutionContext;
    }

    public static void remove() {
        CONTEXT_THREAD_LOCAL.remove();
    }
}
