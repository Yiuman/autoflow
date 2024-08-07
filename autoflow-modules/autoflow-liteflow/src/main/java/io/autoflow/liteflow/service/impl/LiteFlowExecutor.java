package io.autoflow.liteflow.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import io.autoflow.common.http.SSEContext;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.liteflow.utils.LiteFlows;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.FlowExecutionResult;
import lombok.RequiredArgsConstructor;
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

    @Override
    public FlowExecutionResult execute(Flow flow) {
        FlowExecutionResult executionResult = new FlowExecutionResult();
        executionResult.setFlowId(flow.getId());
        String chainId = getExecutableId(flow);
        executionResult.setStartTime(LocalDateTime.now());
        LiteflowResponse liteflowResponse = flowExecutor.execute2Resp(chainId, null, FlowExecutionContext.class);
        executionResult.setEndTime(LocalDateTime.now());
        List<ExecutionResult<ExecutionData>> executionResults = liteflowResponse.getContextBean(FlowExecutionContext.class).getExecutionResults();
        executionResult.setData(executionResults);
        return executionResult;
    }

    @Override
    public String getExecutableId(Flow flow) {
        return LiteFlows.createChain(flow);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ExecutionResult<ExecutionData>> executeNode(Node node) {
        Map<String, List<ExecutionData>> inputData = (Map<String, List<ExecutionData>>) node.getData().get(Constants.INPUT_DATA);
        FlowExecutionContext flowExecutionContext = new FlowExecutionContext();
        flowExecutionContext.getParameters().putAll(node.getData());
        flowExecutionContext.getInputData().putAll(inputData);
        String chainId = getExecutableId(Flow.singleNodeFlow(node));
        LiteflowResponse liteflowResponse = flowExecutor.execute2Resp(chainId, null, flowExecutionContext);
        return liteflowResponse.getContextBean(FlowExecutionContext.class).getExecutionResults();
    }


    @Override
    public void startByExecutableId(String executableId) {
        ThreadUtil.execute(
                () -> {
                    flowExecutor.execute2Resp(executableId, null, FlowExecutionContext.class);
                    SSEContext.close(executableId);
                }
        );
    }
}
