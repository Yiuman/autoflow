package io.autoflow.app.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.core.Services;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.core.utils.Flows;
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
        BpmnModel bpmnModel = Flows.convert(flow);
        Deployment deploy = repositoryService.createDeployment()
                .name(flow.getName())
                .key(flow.getId())
                .addBpmnModel(String.format("%s.bpmn", flow.getName()), bpmnModel)
                .deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploy.getId())
                .singleResult();
        runtimeService.startProcessInstanceById(processDefinition.getId());
        FlowExecutionContext flowExecutionContext = FlowExecutionContext.get();
        return flowExecutionContext.getInputData();
    }

    @Override
    public List<ExecutionData> executeNode(Node node) {
        try {
            io.autoflow.spi.Service service = Services.getService(node.getServiceName());
            Assert.notNull(service, () -> new ExecuteException(String.format("cannot found Service named '%s'", node.getServiceName())));
            Map<String, Object> parameters = Optional.of(node.getData()).orElse(MapUtil.newHashMap());
            ExecutionData executionData = new ExecutionData();
            executionData.setJson(JSONUtil.parseObj(parameters));
            return service.execute(OnceExecutionContext.create(executionData));
        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", node.getServiceName()), throwable);
            return List.of(ExecutionData.error(node.getServiceName(), throwable));
        }

    }
}
