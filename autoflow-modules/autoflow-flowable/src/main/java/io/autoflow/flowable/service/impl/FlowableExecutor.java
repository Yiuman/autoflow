package io.autoflow.flowable.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.core.runtime.ServiceExecutors;
import io.autoflow.flowable.utils.Flows;
import io.autoflow.spi.Services;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.context.FlowContextHolder;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.exception.ExecuteException;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.FlowExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author yiuman
 * @date 2023/7/26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowableExecutor implements Executor {
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    @Override
    public FlowExecutionResult execute(Flow flow) {
        FlowExecutionResult executionResult = new FlowExecutionResult();
        executionResult.setFlowId(flow.getId());
        executionResult.setStartTime(LocalDateTime.now());
        String executableId = getExecutableId(flow);
        runtimeService.startProcessInstanceById(executableId);
        executionResult.setFlowInstId(executableId);
        executionResult.setEndTime(LocalDateTime.now());
        FlowExecutionContextImpl flowExecutionContext = (FlowExecutionContextImpl) FlowContextHolder.get();
        executionResult.setData(flowExecutionContext.getExecutionResults());
        return executionResult;
    }

    @Override
    public String getExecutableId(Flow flow) {
        BpmnModel bpmnModel = Flows.convert(flow);
        Deployment deploy = repositoryService.createDeployment()
                .name(flow.getName())
                .key(flow.getId())
                .addBpmnModel(String.format("%s.bpmn", flow.getName()), bpmnModel)
                .deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploy.getId())
                .singleResult();
        return processDefinition.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ExecutionResult<?>> executeNode(Node node) {
        io.autoflow.spi.Service<Object> service = Services.getService(node.getServiceId());
        Assert.notNull(service, () -> new ExecuteException(String.format("cannot found Service named '%s'", node.getServiceId()), node.getServiceId()));
        List<ExecutionResult<?>> executionResults;
        Map<String, Object> runOnceData = Optional.of(node.getData()).orElse(MapUtil.newHashMap());
        ServiceData serviceData = new ServiceData();
        serviceData.setNodeId(node.getId());
        serviceData.setServiceId(service.getId());
        serviceData.setParameters(runOnceData);
        try {
            Map<String, List<Object>> inputData = (Map<String, List<Object>>) runOnceData.get(Constants.INPUT_DATA);
            if (node.loopIsValid()) {
                ExecutionContext flowExecutionContext = FlowContextHolder.get();
                flowExecutionContext.getInputData().putAll(inputData);
                FlowExecutionResult execute = execute(Flow.singleNodeFlow(node));
                executionResults = execute.getData();
                FlowContextHolder.remove();
            } else {
                runOnceData.remove(Constants.INPUT_DATA);
                executionResults = List.of(
                        ServiceExecutors.execute(serviceData, service, FlowExecutionContextImpl.create(runOnceData, inputData))
                );
            }

        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", node.getServiceId()), throwable);
            executionResults = List.of(ExecutionResult.error(serviceData, throwable));
        }
        return executionResults;

    }

    @Override
    public void startByExecutableId(String executableId) {
        runtimeService.startProcessInstanceById(executableId);
    }

}

