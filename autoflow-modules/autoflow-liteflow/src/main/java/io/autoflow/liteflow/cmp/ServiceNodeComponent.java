package io.autoflow.liteflow.cmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.core.Services;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.context.OnceExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Component
@Slf4j
public class ServiceNodeComponent extends NodeComponent {

    @Override
    public void process() {
        //防止转义异常
        ServiceData serviceData = getCmpData(ServiceData.class);
        Assert.notNull(serviceData);
        String serviceId = serviceData.getServiceId();
        StopWatch stopWatch = new StopWatch(StrUtil.format("【{} Task】", serviceId));
        stopWatch.start();
        Service service = Services.getService(serviceId);
        Assert.notNull(service, () -> new RuntimeException(StrUtil.format("cannot found service named '{}'", serviceId)));
        FlowExecutionContext flowExecutionContext = getContextBean(FlowExecutionContext.class);
        ExecutionData currentExecutionData;
        try {
            OnceExecutionContext onceExecutionContext = new OnceExecutionContext(flowExecutionContext, serviceData.getParameters());
            Map<String, Object> currLoopObj = getCurrLoopObj();
            if (Objects.nonNull(currLoopObj)) {
                onceExecutionContext.getVariables().putAll(currLoopObj);
            }

            currentExecutionData = service.execute(onceExecutionContext);
        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", serviceId), throwable);
            currentExecutionData = ExecutionData.error(serviceId, throwable);
        }

        Map<String, List<ExecutionData>> inputData = flowExecutionContext.getInputData();
        List<ExecutionData> nodeExecutionDataList = Optional
                .ofNullable(inputData.get(getNodeId()))
                .orElse(Collections.synchronizedList(CollUtil.newArrayList()));
        nodeExecutionDataList.add(currentExecutionData);
        inputData.put(getNodeId(), nodeExecutionDataList);
        stopWatch.stop();
        log.debug("\n" + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}
