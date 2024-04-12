package io.autoflow.liteflow.cmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.yomahub.liteflow.aop.ICmpAroundAspect;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.common.http.SSEContext;
import io.autoflow.liteflow.enums.Event;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        log.info(cmp.getNodeId() + "onSuccess");
    }

    @Override
    public void onError(NodeComponent cmp, Exception e) {
        log.info(cmp.getNodeId() + "onError");
        SSEContext.close(cmp.getChainId());
    }

    private void sendData(NodeComponent cmp, Event event) {
        SseEmitter sseEmitter = SSEContext.get(cmp.getChainId());
        if (Objects.isNull(sseEmitter)) {
            return;
        }
        try {
            String sseData = "";
            FlowExecutionContext flowExecutionContext = cmp.getContextBean(FlowExecutionContext.class);
            Map<String, List<ExecutionData>> nodeExecutionDataMap = flowExecutionContext.getInputData();
            String activityId = cmp.getNodeId();
            if (Objects.nonNull(nodeExecutionDataMap)) {
                List<ExecutionData> executionDataList = nodeExecutionDataMap.get(activityId);
                sseData = CollUtil.isEmpty(executionDataList) ? "" : JSONUtil.toJsonStr(executionDataList);
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
