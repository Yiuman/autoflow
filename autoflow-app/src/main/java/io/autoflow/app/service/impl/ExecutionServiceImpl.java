package io.autoflow.app.service.impl;

import cn.hutool.core.lang.Assert;
import io.autoflow.app.model.Workflow;
import io.autoflow.app.model.WorkflowInst;
import io.autoflow.app.service.ExecutionService;
import io.autoflow.app.service.WorkflowInstService;
import io.autoflow.app.service.WorkflowService;
import io.autoflow.core.events.FlowErrorEvent;
import io.autoflow.core.runtime.Executor;
import io.autoflow.spi.context.FlowContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Service;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ExecutionServiceImpl implements ExecutionService {
    private final WorkflowService workflowService;
    private final Executor executor;
    private final WorkflowInstService workflowInstService;

    @Override
    public WorkflowInst execute(String workflowId) {
        Workflow workflow = getWorkflowDefinition(workflowId);
        return execute(workflow);
    }

    @Override
    public WorkflowInst execute(Workflow workflow) {
        WorkflowInst workflowInst = getExecutableFlowInst(workflow);
        executor.execute(workflowInst.getFlow());
        return workflowInst;
    }

    @Override
    public WorkflowInst executeAsyncByWorkflowId(String workflowId) {
        Workflow workflow = getWorkflowDefinition(workflowId);
        return executeAsync(workflow);
    }

    @Override
    public WorkflowInst executeAsyncByWorkflowInstId(String workflowInstId) {
        WorkflowInst workflowInst = workflowInstService.get(workflowInstId);
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor(THREAD_POOL_NAME);
        dtpExecutor.submit(() -> {
            try {
                executor.execute(workflowInst.getFlow());
            } catch (Throwable throwable) {
                log.error("Execute workflow happen error", throwable);
                FlowErrorEvent flowErrorEvent = new FlowErrorEvent();
                flowErrorEvent.setFlowId(workflowInst.getWorkflowId());
                flowErrorEvent.setFlowInstId(workflowInstId);
                executor.getEventDispatcher().dispatch(flowErrorEvent);
            }

        });
        return workflowInst;
    }

    private Workflow getWorkflowDefinition(String workflowId) {
        Workflow workflow = workflowService.get(workflowId);
        Assert.notNull(workflow, "Not find");
        String flowStr = workflow.getFlowStr();
        Assert.notBlank(flowStr);
        return workflow;
    }


    @Override
    public WorkflowInst executeAsync(Workflow workflow) {
        WorkflowInst workflowInst = getExecutableFlowInst(workflow);
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor(THREAD_POOL_NAME);
        dtpExecutor.submit(() -> {
            executor.execute(workflowInst.getFlow());
        });
        return workflowInst;
    }

    @Override
    public WorkflowInst getExecutableFlowInst(Workflow workflow) {
        return workflowInstService.newWorkflowInstance(workflow);
    }

    @Override
    public void stop(String workflowId) {
        FlowContextHolder.interrupt(workflowId);
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }
}
