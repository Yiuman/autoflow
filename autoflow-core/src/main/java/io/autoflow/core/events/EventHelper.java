package io.autoflow.core.events;

import io.autoflow.core.model.Flow;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.FlowExecutionResult;
import io.autoflow.spi.model.ServiceData;

/**
 * @author yiuman
 * @date 2024/11/14
 */
public final class EventHelper {
    private EventHelper() {
    }

    public static FlowStartEvent createFlowStartEvent(Flow flow, ExecutionContext ctx) {
        FlowStartEvent flowEvent = new FlowStartEvent();
        flowEvent.setFlowId(flow.getId());
        flowEvent.setFlow(flow);
        flowEvent.setFlowInstId(flow.getRequestId());
        flowEvent.setContext(ctx);
        return flowEvent;
    }

    public static FlowEndEvent createFlowEndEvent(Flow flow, ExecutionContext ctx, FlowExecutionResult flowExecutionResult) {
        FlowEndEvent flowEvent = new FlowEndEvent();
        flowEvent.setFlowId(flow.getId());
        flowEvent.setFlow(flow);
        flowEvent.setFlowInstId(flow.getRequestId());
        flowEvent.setContext(ctx);
        flowEvent.setFlowExecutionResult(flowExecutionResult);
        return flowEvent;
    }

    public static ServiceStartEvent createServiceStartEvent(ServiceData serviceData, ExecutionContext ctx) {
        ServiceStartEvent serviceEvent = new ServiceStartEvent();
        serviceEvent.setServiceData(serviceData);
        serviceEvent.setContext(ctx);
        return serviceEvent;
    }

    public static <T> ServiceEndEvent createServiceEndEvent(ServiceData serviceData,
                                                            ExecutionContext ctx,
                                                            ExecutionResult<T> executionResult) {
        ServiceEndEvent serviceEvent = new ServiceEndEvent();
        serviceEvent.setServiceData(serviceData);
        serviceEvent.setContext(ctx);
        serviceEvent.setExecutionResult(executionResult);
        return serviceEvent;
    }
}
