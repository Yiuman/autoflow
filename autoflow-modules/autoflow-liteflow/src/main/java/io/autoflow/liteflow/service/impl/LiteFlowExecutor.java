package io.autoflow.liteflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import io.autoflow.core.events.EventHelper;
import io.autoflow.core.events.EventListener;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.liteflow.utils.LiteFlows;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.context.FlowContextHolder;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.FlowExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Service
@RequiredArgsConstructor
public class LiteFlowExecutor implements Executor {

    private final FlowExecutor flowExecutor;

    @Autowired(required = false)
    public void setEventListeners(List<EventListener> eventListeners) {
        if (CollUtil.isEmpty(eventListeners)) {
            return;
        }
        for (EventListener eventListener : eventListeners) {
            getEventDispatcher().addEventListener(eventListener);
        }
    }

    @Override
    public FlowExecutionResult execute(Flow flow) {
        FlowExecutionResult executionResult = new FlowExecutionResult();
        executionResult.setFlowId(flow.getId());
        String chainId = getExecutableId(flow);
        executionResult.setStartTime(LocalDateTime.now());
        ExecutionContext flowExecutionContext = FlowContextHolder.get(flow.getRequestId(), flow.getData());
        getEventDispatcher().dispatch(EventHelper.createFlowStartEvent(flow, flowExecutionContext));
        LiteflowResponse liteflowResponse = flowExecutor.execute2RespWithRid(chainId, this, flow.getRequestId(), flowExecutionContext);
        executionResult.setEndTime(LocalDateTime.now());
        executionResult.setFlowInstId(liteflowResponse.getRequestId());
        List<ExecutionResult<Object>> executionResults = liteflowResponse
                .getContextBean(FlowExecutionContextImpl.class)
                .getExecutionResults();
        executionResult.setData(executionResults);
        getEventDispatcher().dispatch(EventHelper.createFlowEndEvent(flow, flowExecutionContext, executionResult));
        return executionResult;
    }

    @Override
    public String getExecutableId(Flow flow) {
        return LiteFlows.createChain(flow);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ExecutionResult<Object>> executeNode(Node node) {
        FlowExecutionContextImpl flowExecutionContext = new FlowExecutionContextImpl();
        flowExecutionContext.getInputData().putAll((Map<String, Object>) node.getData().get(Constants.INPUT_DATA_ATTR_NAME));
        flowExecutionContext.getVariables().putAll((Map<String, Object>) node.getData().get(Constants.VARIABLES_ATTR_NAME));
        flowExecutionContext.getParameters().putAll(node.getData());
        String chainId = getExecutableId(Flow.singleNodeFlow(node));
        LiteflowResponse liteflowResponse = flowExecutor.execute2Resp(chainId, this, flowExecutionContext);
        return liteflowResponse.getContextBean(FlowExecutionContextImpl.class).getExecutionResults();
    }


    @Override
    public void startByExecutableId(String executableId) {
        ThreadUtil.execute(
                () -> flowExecutor.execute2Resp(executableId, this, FlowExecutionContextImpl.class)
        );
    }

}
