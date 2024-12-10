package io.autoflow.app.service;

import io.autoflow.app.model.Workflow;
import io.autoflow.app.model.WorkflowInst;
import io.autoflow.core.runtime.Executor;

/**
 * @author yiuman
 * @date 2024/11/13
 */
public interface ExecutionService {
    String THREAD_POOL_NAME = "workflow_thread_pool";

    WorkflowInst execute(String workflowId);

    WorkflowInst execute(Workflow workflow);

    WorkflowInst executeAsyncByWorkflowId(String workflowId);

    WorkflowInst executeAsyncByWorkflowInstId(String workflowInstId);

    WorkflowInst executeAsync(Workflow workflow);

    WorkflowInst getExecutableFlowInst(Workflow workflow);

    void stop(String workflowId);

    Executor getExecutor();

}