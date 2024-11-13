package io.autoflow.core.runtime;

import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.FlowExecutionResult;

import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/26
 */
public interface Executor {

    /**
     * 执行流程
     *
     * @param flow 流程定义
     * @return 流程上下文数据
     */
    FlowExecutionResult execute(Flow flow);

    /**
     * 获取可执行的ID
     *
     * @param flow 流程定义
     * @return 可执行的ID（流程定义ID）
     */
    String getExecutableId(Flow flow);

    /**
     * 执行单个节点
     *
     * @param node 节点定义
     * @return 单个节点的数据
     */
    List<ExecutionResult<Object>> executeNode(Node node);

    void startByExecutableId(String executableId);

    default ServiceExecutor getServiceExecutor() {
        return ServiceExecutors.getDefaultServiceExecutor();
    }

}