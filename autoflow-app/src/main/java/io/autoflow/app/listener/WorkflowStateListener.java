package io.autoflow.app.listener;

import io.autoflow.app.enums.FlowState;
import io.autoflow.app.model.WorkflowInst;
import io.autoflow.app.service.WorkflowInstService;
import io.autoflow.core.enums.EventType;
import io.autoflow.core.events.Event;
import io.autoflow.core.events.EventListener;
import io.autoflow.core.events.FlowEndEvent;
import io.autoflow.core.events.FlowStartEvent;
import io.autoflow.spi.model.FlowExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@Component
@RequiredArgsConstructor
public class WorkflowStateListener implements EventListener {
    private final WorkflowInstService workflowInstService;

    @Override
    public void onEvent(Event event) {
        if (event instanceof FlowStartEvent flowStartEvent) {
            String flowInstId = flowStartEvent.getFlowInstId();
            WorkflowInst workflowInst = workflowInstService.get(flowInstId);
            workflowInst.setFlowState(FlowState.RUNNING);
            workflowInstService.save(workflowInst);
        }

        if (event instanceof FlowEndEvent flowEndEvent) {
            String flowInstId = flowEndEvent.getFlowInstId();
            FlowExecutionResult flowExecutionResult = flowEndEvent.getFlowExecutionResult();
            WorkflowInst workflowInst = workflowInstService.get(flowInstId);
            workflowInst.setStartTime(flowExecutionResult.getStartTime());
            workflowInst.setEndTime(flowExecutionResult.getEndTime());
            workflowInst.setDurationMs(flowExecutionResult.getDurationMs());
            workflowInst.setFlowState(FlowState.END);
            workflowInstService.save(workflowInst);
        }
    }

    @Override
    public Collection<? extends EventType> getTypes() {
        return List.of(EventType.FlOW_START, EventType.FLOW_END);
    }
}
