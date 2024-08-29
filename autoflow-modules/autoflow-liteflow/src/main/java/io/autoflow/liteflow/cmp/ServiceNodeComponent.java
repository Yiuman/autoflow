package io.autoflow.liteflow.cmp;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.core.Services;
import io.autoflow.core.runtime.ServiceExecutors;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.context.OnceExecutionContext;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@SuppressWarnings("unchecked")
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

        Service<Object> service = Services.getService(serviceId);
        Assert.notNull(service, () -> new RuntimeException(StrUtil.format("cannot found service named '{}'", serviceId)));
        FlowExecutionContextImpl flowExecutionContext = getContextBean(FlowExecutionContextImpl.class);
        //把当前的节点的ID作为key 参数作为值放下环境变量中后续其他节点引用
        flowExecutionContext.getVariables().put(getNodeId(), serviceData.getParameters());
        ExecutionResult<Object> executionResult;
        try {
            OnceExecutionContext onceExecutionContext = new OnceExecutionContext(flowExecutionContext, serviceData.getParameters());
            Map<String, Object> currLoopObj = getCurrLoopObj();
            if (Objects.nonNull(currLoopObj)) {
                onceExecutionContext.getVariables().putAll(currLoopObj);
            }
            executionResult = ServiceExecutors.execute(serviceData, service, onceExecutionContext);
        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", serviceId), throwable);
            executionResult = ExecutionResult.error(serviceData, throwable);
        }

        flowExecutionContext.addExecutionResult(executionResult);
        stopWatch.stop();
        log.debug("\n" + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

}
