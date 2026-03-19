package io.autoflow.agent.executor;

import io.autoflow.agent.NodeExecutor;
import io.autoflow.spi.Service;
import io.autoflow.spi.Services;
import io.autoflow.spi.context.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class NodeExecutorImpl implements NodeExecutor {

    @Override
    public Object execute(String nodeId, Map<String, Object> args) {
        Service<?> service = Services.getService(nodeId);
        if (service == null) {
            throw new RuntimeException("Service not found: " + nodeId);
        }

        ExecutionContext context = new SimpleExecutionContext(args != null ? args : new HashMap<>());
        return service.execute(context);
    }

    private static class SimpleExecutionContext implements ExecutionContext {
        private final Map<String, Object> parameters = new HashMap<>();
        private final Map<String, Object> inputData = new HashMap<>();
        private final Map<String, Object> variables = new HashMap<>();

        SimpleExecutionContext(Map<String, Object> args) {
            this.parameters.putAll(args);
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
            return parameters.get(key);
        }

        @Override
        public ExecutionContext getParent() {
            return null;
        }
    }
}
