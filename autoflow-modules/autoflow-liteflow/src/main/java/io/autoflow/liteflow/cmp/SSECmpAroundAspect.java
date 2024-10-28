package io.autoflow.liteflow.cmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.yomahub.liteflow.aop.ICmpAroundAspect;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.common.http.SSEContext;
import io.autoflow.liteflow.enums.Event;
import io.autoflow.plugin.loopeachitem.LoopItem;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.model.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/4/12
 */
@Component
@Slf4j
public class SSECmpAroundAspect implements ICmpAroundAspect {

    @Override
    public void beforeProcess(NodeComponent cmp) {
        sendData(cmp, Event.ACTIVITY_STARTED);
    }

    @Override
    public void afterProcess(NodeComponent cmp) {
        sendData(cmp, Event.ACTIVITY_COMPLETED);
    }

    @Override
    public void onSuccess(NodeComponent cmp) {
        log.debug(cmp.getNodeId() + "onSuccess");
    }

    @Override
    public void onError(NodeComponent cmp, Exception e) {
        log.debug(cmp.getNodeId() + "onError");
        SSEContext.close(cmp.getChainId());
    }

    private void sendData(NodeComponent cmp, Event event) {
        SseEmitter sseEmitter = SSEContext.get(cmp.getChainId());
        if (Objects.isNull(sseEmitter)) {
            return;
        }
        try {
            String sseData = "";
            FlowExecutionContextImpl flowExecutionContext = cmp.getContextBean(FlowExecutionContextImpl.class);
            Map<String, List<ExecutionResult<Object>>> nodeExecutionResultMap = flowExecutionContext.getNodeExecutionResultMap();
            String activityId = cmp.getNodeId();
            LoopItem loopItem = cmp.getCurrLoopObj();
            if (Objects.nonNull(nodeExecutionResultMap) && Event.ACTIVITY_COMPLETED == event) {
                List<ExecutionResult<Object>> executionResults = nodeExecutionResultMap.get(activityId);
                if (Objects.nonNull(loopItem)) {
                    executionResults = executionResults.stream().filter(executionResult
                                    -> Objects.equals(executionResult.getLoopCounter(), loopItem.getLoopCounter())
                                    && Objects.equals(executionResult.getLoopId(), loopItem.getId()))
                            .collect(Collectors.toList());
                }

                if (CollUtil.isNotEmpty(executionResults)) {
                    sseData = CollUtil.isEmpty(executionResults) ? "" : JSONUtil.toJsonStr(CollUtil.newArrayList(executionResults));
                }

            }

            SseEmitter.SseEventBuilder data = SseEmitter.event()
                    .id(activityId)
                    .name(event.name())
                    .data(sseData);
            sseEmitter.send(data);
            log.debug("Match executeService send sse " + event + " activityId:" + activityId);
        } catch (Throwable throwable) {
            log.error("SSE send data happen error", throwable);
        }
    }
}
