package io.autoflow.app.listener;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.model.ExecutionInst;
import io.autoflow.app.service.ExecutionInstService;
import io.autoflow.core.enums.EventType;
import io.autoflow.core.events.Event;
import io.autoflow.core.events.EventListener;
import io.autoflow.core.events.ServiceEndEvent;
import io.autoflow.spi.model.ExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2024/11/13
 */
@RequiredArgsConstructor
@Component
public class SaveServiceExecutionDataListener implements EventListener {

    private final ExecutionInstService executionInstService;


    /**
     * 保存执行实例
     *
     * @param executionResult 执行结果
     * @param <T>             数据类型
     */
    private <T> void saveExecutionInst(ExecutionResult<T> executionResult) {
        ExecutionInst executionInst = new ExecutionInst();
        executionInst.setWorkflowId(executionResult.getFlowId());
        executionInst.setWorkflowInstId(executionResult.getFlowInstId());
        executionInst.setNodeId(executionResult.getNodeId());
        executionInst.setServiceId(executionResult.getServiceId());
        executionInst.setLoopId(executionResult.getLoopId());
        executionInst.setLoopIndex(executionResult.getLoopIndex());
        executionInst.setNrOfInstances(executionResult.getNrOfInstances());
        executionInst.setData(JSONUtil.toJsonStr(executionResult.getData()));
        executionInst.setStartTime(executionResult.getStartTime());
        executionInst.setEndTime(executionResult.getEndTime());
        executionInst.setDurationMs(executionResult.getDurationMs());
        if (Objects.nonNull(executionResult.getError())) {
            executionInst.setErrorMessage(executionResult.getError().getMessage());
        }

        executionInstService.save(executionInst);
    }

    @Override
    public void onEvent(Event event) {
        ServiceEndEvent serviceEndEvent = (ServiceEndEvent) event;
        saveExecutionInst(serviceEndEvent.getExecutionResult());
    }

    @Override
    public Collection<? extends EventType> getTypes() {
        return List.of(EventType.SERVICE_END);
    }
}
