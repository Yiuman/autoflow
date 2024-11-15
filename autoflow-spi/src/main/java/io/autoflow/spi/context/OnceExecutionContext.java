package io.autoflow.spi.context;

import io.autoflow.spi.provider.ExecutionContextValueProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/15
 */
public class OnceExecutionContext implements ExecutionContext {
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, Object> inputData = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private final ExecutionContextValueProvider executionContextValueProvider;
    private final ExecutionContext parent;

    public OnceExecutionContext(ExecutionContext executionContext) {
        this.variables.putAll(executionContext.getVariables());
        this.inputData.putAll(executionContext.getInputData());
        this.executionContextValueProvider = new ExecutionContextValueProvider(this);
        this.parent = executionContext;
    }

    public OnceExecutionContext(ExecutionContext executionContext, Map<String, Object> parameters) {
        this.variables.putAll(executionContext.getVariables());
        this.inputData.putAll(executionContext.getInputData());
        this.executionContextValueProvider = new ExecutionContextValueProvider(this);
        this.parameters.putAll(parameters);
        this.parent = executionContext;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, Object> getInputData() {
        return inputData;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public Object parseValue(String key) {
        return executionContextValueProvider.get(key);
    }

    @Override
    public ExecutionContext getParent() {
        return parent;
    }
}
