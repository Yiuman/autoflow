package io.autoflow.app.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.common.http.SSEContext;
import io.autoflow.core.enums.EventType;
import io.autoflow.core.events.Event;
import io.autoflow.core.events.EventListener;
import io.autoflow.core.events.ServiceEndEvent;
import io.autoflow.core.events.ServiceStartEvent;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
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
    }

    @Override
    public Collection<? extends EventType> getTypes() {
        return List.of(EventType.SERVICE_START, EventType.SERVICE_END);
    }
}
