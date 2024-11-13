package io.autoflow.flowable.delegate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.common.http.SSEContext;
import io.autoflow.spi.context.FlowContextHolder;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.model.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.delegate.event.impl.FlowableProcessEventImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author yiuman
 * @date 2024/3/29
 */
@Slf4j
public class ExecuteServiceListener implements FlowableEventListener {

    private final Map<FlowableEngineEventType, Consumer<FlowableEvent>> engineEventTypeConsumerMap = new HashMap<>() {{
        put(FlowableEngineEventType.ACTIVITY_STARTED, ExecuteServiceListener::sseSendData);
        put(FlowableEngineEventType.ACTIVITY_COMPLETED, ExecuteServiceListener::sseSendData);
        put(FlowableEngineEventType.PROCESS_COMPLETED, (event) -> {
            sseSendData(event);
            FlowableProcessEventImpl flowableProcessEvent = (FlowableProcessEventImpl) event;
            //可能是子流程
            if (Objects.isNull(flowableProcessEvent.getExecution().getParent())) {
                SSEContext.close(flowableProcessEvent.getProcessDefinitionId());
            }

        });
    }};

    private static void sseSendData(FlowableEvent event) {
        FlowableProcessEventImpl entityEvent = (FlowableProcessEventImpl) event;
        ExecutionEntityImpl execution = (ExecutionEntityImpl) entityEvent.getExecution();
        SseEmitter sseEmitter = SSEContext.get(execution.getProcessDefinitionId());
        if (Objects.isNull(sseEmitter)) {
            return;
        }

        try {
            String sseData = "";
            FlowExecutionContext flowExecutionContext = (FlowExecutionContext) FlowContextHolder.get();
            Map<String, List<ExecutionResult<Object>>> nodeExecutionResultMap = flowExecutionContext.getNodeExecutionResultMap();
            String activityId = execution.getActivityId();
            if (Objects.nonNull(nodeExecutionResultMap)) {
                List<ExecutionResult<Object>> executionDataList = nodeExecutionResultMap.get(activityId);
                sseData = CollUtil.isEmpty(executionDataList) ? "" : JSONUtil.toJsonStr(executionDataList);
            }

            SseEmitter.SseEventBuilder data = SseEmitter.event()
                    .id(activityId)
                    .name(event.getType().name())
                    .data(sseData);
            sseEmitter.send(data);
            log.debug("Match executeService send sse " + event.getType() + " activityId:" + activityId);
        } catch (Throwable throwable) {
            log.error("SSE send data happen error", throwable);
        }
    }

    @Override
    public void onEvent(FlowableEvent event) {
        Consumer<FlowableEvent> flowableEventConsumer = engineEventTypeConsumerMap.get(event.getType());
        if (Objects.nonNull(flowableEventConsumer)) {
            flowableEventConsumer.accept(event);
        }

    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}

