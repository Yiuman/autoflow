package io.autoflow.spi.context;

import cn.hutool.core.thread.threadlocal.NamedThreadLocal;
import io.autoflow.spi.model.ExecutionData;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author yiuman
 * @date 2023/7/14
 */
@Data
public class FlowExecutionContext implements ExecutionContext {
    private static final ThreadLocal<FlowExecutionContext> CONTEXT_THREAD_LOCAL = new NamedThreadLocal<>(FlowExecutionContext.class.getName());
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, List<ExecutionData>> inputData = new HashMap<>();

    public static FlowExecutionContext get() {
        FlowExecutionContext flowExecutionContext = Optional.ofNullable(CONTEXT_THREAD_LOCAL.get())
                .orElse(new FlowExecutionContext());
        CONTEXT_THREAD_LOCAL.set(flowExecutionContext);
        return flowExecutionContext;
    }

    public static void remove() {
        CONTEXT_THREAD_LOCAL.remove();
    }

    @Override
    public Map<String, List<ExecutionData>> getInputData() {
        return inputData;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

}
