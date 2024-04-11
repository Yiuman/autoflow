package io.autoflow.flowable.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.core.Services;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.flowable.utils.Flows;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.context.OnceExecutionContext;
import io.autoflow.spi.exception.ExecuteException;
import io.autoflow.spi.model.ExecutionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

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
    public Map<String, List<ExecutionData>> execute(Flow flow) {
        runtimeService.startProcessInstanceById(getExecutableId(flow));
        FlowExecutionContext flowExecutionContext = FlowExecutionContext.get();
        return flowExecutionContext.getInputData();
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
    public List<ExecutionData> executeNode(Node node) {
        io.autoflow.spi.Service service = Services.getService(node.getServiceId());
        Assert.notNull(service, () -> new ExecuteException(String.format("cannot found Service named '%s'", node.getServiceId()), node.getServiceId()));
        try {
            Map<String, Object> runOnceData = Optional.of(node.getData()).orElse(MapUtil.newHashMap());
            Map<String, List<ExecutionData>> inputData = (Map<String, List<ExecutionData>>) runOnceData.get(Constants.INPUT_DATA);
            if (node.loopIsValid()) {
                FlowExecutionContext flowExecutionContext = FlowExecutionContext.get();
                flowExecutionContext.getInputData().putAll(inputData);
                Map<String, List<ExecutionData>> execute = execute(Flow.singleNodeFlow(node));
                return execute.get(node.getId());
            } else {
                runOnceData.remove(Constants.INPUT_DATA);
                OnceExecutionContext onceExecutionContext = OnceExecutionContext.create(runOnceData, inputData);
                return List.of(service.execute(onceExecutionContext));
            }

        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", node.getServiceId()), throwable);
            return List.of(ExecutionData.error(node.getServiceId(), throwable));
        }

    }

    @Override
    public void startByExecutableId(String executableId) {
        runtimeService.startProcessInstanceById(executableId);
    }
}
