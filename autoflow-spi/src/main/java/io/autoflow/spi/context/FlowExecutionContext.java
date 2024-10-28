package io.autoflow.spi.context;

import io.autoflow.spi.model.ExecutionResult;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/8/29
 */
public interface FlowExecutionContext extends ExecutionContext {

    /**
     * 获取执行的结果
     *
     * @return 执行的结果
     */
    List<ExecutionResult<Object>> getExecutionResults();

    /**
     * 获取节点与节点的执行结果集映射
     *
     * @return 节点与节点的执行结果集映射
     */
    Map<String, List<ExecutionResult<Object>>> getNodeExecutionResultMap();

    /**
     * 添加执行结果
     *
     * @param executionResult 执行结果
     */
    void addExecutionResult(ExecutionResult<Object> executionResult);

    Map<String, ExecutionContext> getLoopContextMap();
}