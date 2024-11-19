package io.autoflow.app.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.common.http.SSEContext;
import io.autoflow.core.enums.EventType;
import io.autoflow.core.events.*;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@Component
@Slf4j
public class ServiceSSEListener implements EventListener {

    private void sendServiceStartEvent(ServiceStartEvent serviceStartEvent) {
        ServiceData serviceData = serviceStartEvent.getServiceData();
        SseEmitter sseEmitter = SSEContext.get(serviceData.getFlowInstId());
        if (Objects.nonNull(sseEmitter)) {
            try {
                sseEmitter.send(SseEmitter.event()
                        .id(serviceData.getNodeId())
                        .name(serviceStartEvent.name())
                        .data(""));
            } catch (Throwable throwable) {
                log.warn("sse send service start data error", throwable);
            }
        }
    }

    private void sendServiceEndEvent(ServiceEndEvent serviceEndEvent) {
        ServiceData serviceData = serviceEndEvent.getServiceData();
        SseEmitter sseEmitter = SSEContext.get(serviceData.getFlowInstId());
        if (Objects.nonNull(sseEmitter)) {
            ExecutionResult<?> executionResult = serviceEndEvent.getExecutionResult();
            try {
                sseEmitter.send(
                        SseEmitter.event()
                                .id(serviceData.getNodeId())
                                .name(serviceEndEvent.name())
                                .data(JSONUtil.toJsonStr(CollUtil.newArrayList(executionResult)))
                );
            } catch (Throwable throwable) {
                log.warn("sse send service end data error", throwable);
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ServiceStartEvent serviceStartEvent) {
            sendServiceStartEvent(serviceStartEvent);
        }

        if (event instanceof ServiceEndEvent serviceEndEvent) {
            sendServiceEndEvent(serviceEndEvent);
        }

        if (event instanceof FlowEndEvent flowEndEvent) {
            SSEContext.close(flowEndEvent.getFlowInstId());
        }
    }

    @Override
    public Collection<? extends EventType> getTypes() {
        return List.of(EventType.SERVICE_START, EventType.SERVICE_END, EventType.FLOW_END);
    }
}
