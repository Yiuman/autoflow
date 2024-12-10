package io.autoflow.app.service;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.enums.FlowState;
import io.autoflow.app.model.Workflow;
import io.autoflow.app.model.WorkflowInst;
import io.autoflow.core.model.Flow;
import io.ola.crud.service.CrudService;

import java.time.LocalDateTime;

/**
 * @author yiuman
 * @date 2024/11/13
 */
public interface WorkflowInstService extends CrudService<WorkflowInst> {

    default WorkflowInst newWorkflowInstance(Workflow workflow) {
        WorkflowInst workflowInst = new WorkflowInst();
        workflowInst.setWorkflowId(workflow.getId());
        workflowInst.setSubmitTime(LocalDateTime.now());
        workflowInst.setFlowStr(workflow.getFlowStr());
        workflowInst.setFlowState(FlowState.CREATED);
        save(workflowInst);
        Flow flow = JSONUtil.toBean(workflow.getFlowStr(), Flow.class);
        flow.setRequestId(workflowInst.getId());
        workflowInst.setFlow(flow);
        return workflowInst;
    }

}