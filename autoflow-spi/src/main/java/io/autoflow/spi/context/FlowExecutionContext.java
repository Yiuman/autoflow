package io.autoflow.spi.context;

import io.autoflow.spi.model.ExecutionData;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程执行上下文
 *
 * @author yiuman
 * @date 2023/7/14
 */
@Data
public class FlowExecutionContext implements ExecutionContext {

    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, List<ExecutionData>> inputData = new HashMap<>();

    @Override
    public Map<String, List<ExecutionData>> getInputData() {
        return inputData;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

}
