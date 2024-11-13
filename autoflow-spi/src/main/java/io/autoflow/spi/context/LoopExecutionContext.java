package io.autoflow.spi.context;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/11/13
 */
public class LoopExecutionContext extends OnceExecutionContext {

    public LoopExecutionContext(ExecutionContext executionContext) {
        super(executionContext);
    }

    public LoopExecutionContext(ExecutionContext executionContext, Map<String, Object> parameters) {
        super(executionContext, parameters);
    }
}
