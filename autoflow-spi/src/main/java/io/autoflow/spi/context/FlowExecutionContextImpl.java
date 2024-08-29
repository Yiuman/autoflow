package io.autoflow.spi.context;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import lombok.Data;

import java.util.*;

/**
 * 流程执行上下文
 *
 * @author yiuman
 * @date 2023/7/14
 */
@Data
public class FlowExecutionContextImpl implements FlowExecutionContext {
    private final List<ExecutionResult<Object>> executionResults = Collections.synchronizedList(CollUtil.newArrayList());
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, List<Object>> inputData = new HashMap<>();
    private final Map<String, List<ExecutionResult<Object>>> nodeExecutionResultMap = new HashMap<>();
    private final ExecutionContextValueProvider executionContextValueProvider = new ExecutionContextValueProvider(this);

    public static FlowExecutionContextImpl create(Map<String, Object> parameters) {
        FlowExecutionContextImpl flowExecutionContext = new FlowExecutionContextImpl();
        flowExecutionContext.getParameters().putAll(parameters);
        return flowExecutionContext;
    }

    public static FlowExecutionContextImpl create(Map<String, Object> parameters, Map<String, List<Object>> inputData) {
        FlowExecutionContextImpl flowExecutionContext = new FlowExecutionContextImpl();
        flowExecutionContext.getParameters().putAll(parameters);
        flowExecutionContext.getInputData().putAll(inputData);
        return flowExecutionContext;
    }

    public static <T> FlowExecutionContextImpl create(T parameters) {
        FlowExecutionContextImpl flowExecutionContext = new FlowExecutionContextImpl();
        flowExecutionContext.getParameters().putAll(BeanUtil.beanToMap(parameters));
        return flowExecutionContext;
    }

    @Override
    public Map<String, List<Object>> getInputData() {
        return inputData;
    }

    @Override
    public void addExecutionResult(ExecutionResult<Object> executionResult) {
        executionResults.add(executionResult);
        //输入数据处理
        List<Object> executionData = Optional.ofNullable(getInputData()
                        .get(executionResult.getNodeId()))
                .orElseGet(() -> Collections.synchronizedList(CollUtil.newArrayList()));
        executionData.add(executionResult.getData());
        getInputData().put(executionResult.getNodeId(), executionData);

        //结果集处理
        List<ExecutionResult<Object>> nodeExecutionResults = Optional.ofNullable(getNodeExecutionResultMap().get(executionResult.getNodeId()))
                .orElseGet(() -> Collections.synchronizedList(CollUtil.newArrayList()));
        nodeExecutionResults.add(executionResult);
        getNodeExecutionResultMap().put(executionResult.getNodeId(), nodeExecutionResults);
    }

    @Override
    public Object parseValue(String key) {
        return executionContextValueProvider.get(key.trim());
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

}
