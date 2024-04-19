package io.autoflow.spi.context;

import cn.hutool.core.bean.BeanUtil;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
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
    private final ExecutionContextValueProvider executionContextValueProvider = new ExecutionContextValueProvider(this);

    public static FlowExecutionContext create(Map<String, Object> parameters) {
        FlowExecutionContext flowExecutionContext = new FlowExecutionContext();
        flowExecutionContext.getParameters().putAll(parameters);
        return flowExecutionContext;
    }

    public static FlowExecutionContext create(Map<String, Object> parameters, Map<String, List<ExecutionData>> inputData) {
        FlowExecutionContext flowExecutionContext = new FlowExecutionContext();
        flowExecutionContext.getParameters().putAll(parameters);
        flowExecutionContext.getInputData().putAll(inputData);
        return flowExecutionContext;
    }

    public static <T> FlowExecutionContext create(T parameters) {
        FlowExecutionContext flowExecutionContext = new FlowExecutionContext();
        flowExecutionContext.getParameters().putAll(BeanUtil.beanToMap(parameters));
        return flowExecutionContext;
    }

    @Override
    public Map<String, List<ExecutionData>> getInputData() {
        return inputData;
    }

    @Override
    public Object parseValue(String key) {
        return executionContextValueProvider.get(key);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

}
