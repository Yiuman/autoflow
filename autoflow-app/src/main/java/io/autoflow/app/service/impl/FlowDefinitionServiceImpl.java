package io.autoflow.app.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import io.autoflow.app.entity.FlowDefinition;
import io.autoflow.app.mapper.FlowDefinitionMapper;
import io.autoflow.app.service.FlowDefinitionService;
import io.autoflow.core.utils.Flows;
import lombok.RequiredArgsConstructor;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yiuman
 * @date 2023/7/26
 */
@Service
@RequiredArgsConstructor
public class FlowDefinitionServiceImpl extends ServiceImpl<FlowDefinitionMapper, FlowDefinition>
        implements FlowDefinitionService {
    private final RepositoryService repositoryService;

    @Transactional
    @Override
    public String deploy(FlowDefinition flowDefinition) {
        BpmnModel bpmnModel = Flows.convert(flowDefinition.getFlowJson());
        Deployment deploy = repositoryService.createDeployment()
                .addBpmnModel(String.format("%s.bpmn", flowDefinition.getName()), bpmnModel)
                .name(flowDefinition.getName())
                .deploy();
        flowDefinition.setProcessDefinitionId(deploy.getId());
        flowDefinition.setProcessDefinitionKey(deploy.getKey());
        saveOrUpdate(flowDefinition);
        return flowDefinition.getId();
    }

    @Override
    public Page<FlowDefinition> page(Page<FlowDefinition> page, QueryWrapper query) {
        return super.page(page, query);
    }
}
