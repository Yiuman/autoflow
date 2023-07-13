package io.autoflow.spi.context;

import io.autoflow.spi.model.ExecutionData;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 单个执行的上下文
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
public final class OnceExecutionContext implements ExecutionContext {
    private final Map<String, Object> parameters = new HashMap<>() {{
        put(Constants.INPUT_NAME, Constants.DEFAULT_INPUT_NAME);
        put(Constants.INPUT_INDEX, Constants.DEFAULT_INPUT_INDEX);
    }};
    private Map<String, ExecutionData[]> inputData = new HashMap<>();

    public static OnceExecutionContext create(ExecutionData[] executionData) {
        OnceExecutionContext onceExecutionContext = new OnceExecutionContext();
        onceExecutionContext.getInputData().put(Constants.DEFAULT_INPUT_NAME, executionData);
        return onceExecutionContext;
    }

    public static OnceExecutionContext create(ExecutionData executionData) {
        return create(new ExecutionData[]{executionData});
    }
}
