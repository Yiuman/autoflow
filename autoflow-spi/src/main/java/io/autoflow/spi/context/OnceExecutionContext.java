package io.autoflow.spi.context;

import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.provider.ExecutionContextValueProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/15
 */
public class OnceExecutionContext implements ExecutionContext {
    private final ExecutionContext executionContext;
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private final ExecutionContextValueProvider executionContextValueProvider;

    public OnceExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        this.variables.putAll(executionContext.getVariables());
        this.executionContextValueProvider = new ExecutionContextValueProvider(this);
    }

    public OnceExecutionContext(ExecutionContext executionContext, Map<String, Object> parameters) {
        this.executionContext = executionContext;
        this.variables.putAll(executionContext.getVariables());
        this.executionContextValueProvider = new ExecutionContextValueProvider(this);
        this.parameters.putAll(parameters);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, List<ExecutionData>> getInputData() {
        return executionContext.getInputData();
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
    public List<ExecutionResult<ExecutionData>> getExecutionResults() {
        return executionContext.getExecutionResults();
    }

    @Override
    public void addExecutionResult(ExecutionResult<ExecutionData> executionResult) {
        executionContext.addExecutionResult(executionResult);
    }

    @Override
    public Map<String, List<ExecutionResult<ExecutionData>>> getNodeExecutionResultMap() {
        return executionContext.getNodeExecutionResultMap();
    }
}
