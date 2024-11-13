package io.autoflow.app.service.impl;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.model.ExecutionInst;
import io.autoflow.app.service.ExecutionInstService;
import io.autoflow.core.runtime.ServiceExecutors;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * @author yiuman
 * @date 2024/11/13
 */
@RequiredArgsConstructor
public class ServiceExecutorImpl extends ServiceExecutors.DefaultServiceExecutor {

    private final ExecutionInstService executionInstService;

    @Override
    public <T> ExecutionResult<T> execute(ServiceData serviceData, Service<T> service, ExecutionContext executionContext) {
        ExecutionResult<T> executionResult = super.execute(serviceData, service, executionContext);
        saveExecutionInst(executionResult);
        return executionResult;
    }

    /**
     * 保存执行实例
     *
     * @param executionResult 执行结果
     * @param <T>             数据类型
     */
    private <T> void saveExecutionInst(ExecutionResult<T> executionResult) {
        ExecutionInst executionInst = new ExecutionInst();
        executionInst.setWorkflowId(executionResult.getFlowId());
        executionInst.setWorkflowInstId(executionResult.getFlowInstId());
        executionInst.setNodeId(executionResult.getNodeId());
        executionInst.setServiceId(executionResult.getServiceId());
        executionInst.setLoopId(executionResult.getLoopId());
        executionInst.setLoopCounter(executionResult.getLoopCounter());
        executionInst.setNrOfInstances(executionResult.getNrOfInstances());
        executionInst.setData(JSONUtil.toJsonStr(executionResult.getData()));
        executionInst.setStartTime(executionResult.getStartTime());
        executionInst.setEndTime(executionResult.getEndTime());
        executionInst.setDurationMs(executionResult.getDurationMs());
        if (Objects.nonNull(executionResult.getError())) {
            executionInst.setErrorMessage(executionResult.getError().getMessage());
        }

        executionInstService.save(executionInst);
    }
}
