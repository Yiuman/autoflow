package io.autoflow.app.chat;

import io.autoflow.spi.context.ExecutionContext;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple implementation of ExecutionContext for tool execution in chat.
 * Provides basic context functionality without workflow-specific features.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
public class SimpleExecutionContext implements ExecutionContext {

    private final Map<String, Object> parameters;
    private final Map<String, Object> inputData;
    private final Map<String, Object> variables;

    /**
     * Creates a new SimpleExecutionContext with the given parameters.
     *
     * @param params the parameters for execution
     */
    public SimpleExecutionContext(final Map<String, Object> params) {
        this.parameters = Objects.nonNull(params) ? new HashMap<>(params) : new HashMap<>();
        this.inputData = new HashMap<>();
        this.variables = new HashMap<>();
    }

    /**
     * Creates a new SimpleExecutionContext with empty parameters.
     */
    public SimpleExecutionContext() {
        this(new HashMap<>());
    }

    @Override
    public Object parseValue(final String key) {
        if (parameters.containsKey(key)) {
            return parameters.get(key);
        }
        if (inputData.containsKey(key)) {
            return inputData.get(key);
        }
        if (variables.containsKey(key)) {
            return variables.get(key);
        }
        return null;
    }
}
