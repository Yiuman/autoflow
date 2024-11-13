package io.autoflow.app.service;

import io.autoflow.spi.model.FlowExecutionResult;

/**
 * @author yiuman
 * @date 2024/11/13
 */
public interface ExecutionService {

    FlowExecutionResult executeByWorkflowId(String workflowId);


}