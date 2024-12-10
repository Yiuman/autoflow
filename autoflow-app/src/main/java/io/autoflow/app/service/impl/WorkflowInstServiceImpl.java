package io.autoflow.app.service.impl;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.model.WorkflowInst;
import io.autoflow.app.service.WorkflowInstService;
import io.autoflow.core.model.Flow;
import io.ola.crud.service.impl.BaseService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author yiuman
 * @date 2024/11/13
 */
@Service
public class WorkflowInstServiceImpl extends BaseService<WorkflowInst> implements WorkflowInstService {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends WorkflowInst, ID extends Serializable> T get(ID id) {
        WorkflowInst workflowInst = super.get(id);
        Flow flow = JSONUtil.toBean(workflowInst.getFlowStr(), Flow.class);
        flow.setRequestId(workflowInst.getId());
        workflowInst.setFlow(flow);
        return (T) workflowInst;
    }
}
