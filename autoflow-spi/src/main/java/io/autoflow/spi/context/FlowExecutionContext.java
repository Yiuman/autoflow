package io.autoflow.spi.context;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import io.autoflow.spi.model.ExecutionData;
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
public class FlowExecutionContext implements ExecutionContext {
    private final List<ExecutionResult<ExecutionData>> executionResults = Collections.synchronizedList(CollUtil.newArrayList());
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, List<ExecutionData>> inputData = new HashMap<>();
    private final Map<String, List<ExecutionResult<ExecutionData>>> nodeExecutionResultMap = new HashMap<>();
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
    public void addExecutionResult(ExecutionResult<ExecutionData> executionResult) {
        executionResults.add(executionResult);
        //输入数据处理
        List<ExecutionData> executionData = Optional.ofNullable(getInputData().get(executionResult.getNodeId()))
                .orElseGet(() -> Collections.synchronizedList(CollUtil.newArrayList()));
        executionData.add(executionResult.getData());
        getInputData().put(executionResult.getNodeId(), executionData);

        //结果集处理
        List<ExecutionResult<ExecutionData>> nodeExecutionResults = Optional.ofNullable(getNodeExecutionResultMap().get(executionResult.getNodeId()))
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
