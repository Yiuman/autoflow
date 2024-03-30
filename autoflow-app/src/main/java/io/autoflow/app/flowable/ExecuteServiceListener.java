package io.autoflow.app.flowable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.model.ExecutionData;
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
        put(FlowableEngineEventType.PROCESS_COMPLETED, ExecuteServiceListener::sseSendData);
    }};

    @SuppressWarnings("unchecked")
    private static void sseSendData(FlowableEvent event) {
        FlowableProcessEventImpl entityEvent = (FlowableProcessEventImpl) event;
        ExecutionEntityImpl execution = (ExecutionEntityImpl) entityEvent.getExecution();
        String processDefinitionKey = execution.getProcessDefinitionKey();
        SseEmitter sseEmitter = SSEContext.get(processDefinitionKey);
        if (Objects.nonNull(sseEmitter)) {
            try {
                Map<String, Object> transientVariables = execution.getTransientVariables();
                Map<String, List<ExecutionData>> nodeExecutionDataMap = (Map<String, List<ExecutionData>>) transientVariables.get(Constants.INPUT_DATA);
                String activityId = execution.getActivityId();
                String sseData = "";
                if (Objects.nonNull(nodeExecutionDataMap)) {
                    ExecutionData executionData = CollUtil.getLast(nodeExecutionDataMap.get(activityId));
                    if (Objects.nonNull(executionData)) {
                        sseData = JSONUtil.toJsonStr(executionData);
                    }

                }

                sseEmitter.send(SseEmitter.event()
                        .id(activityId)
                        .name(event.getType().name())
                        .data(sseData));
                log.debug("Match executeService send sse" + event.getType() + " nodeId:" + activityId);
            } catch (Throwable throwable) {
                log.error("SSE send data happen error", throwable);
            }
        }
        if (FlowableEngineEventType.PROCESS_COMPLETED == event.getType()) {
            SSEContext.close(processDefinitionKey);
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
        throw new UnsupportedOperationException("Method not implemented.");
    }
}
