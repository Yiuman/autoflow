package io.autoflow.liteflow.cmp;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.core.events.EventDispatcher;
import io.autoflow.core.events.EventHelper;
import io.autoflow.core.runtime.Executor;
import io.autoflow.core.runtime.ServiceExecutor;
import io.autoflow.liteflow.utils.LiteFlows;
import io.autoflow.plugin.loopeachitem.LoopItem;
import io.autoflow.spi.Service;
import io.autoflow.spi.Services;
import io.autoflow.spi.context.ContextUtils;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        // 防止转义异常
        ServiceData serviceData = getCmpData(ServiceData.class);
        Assert.notNull(serviceData);
        serviceData.setFlowInstId(getSlot().getRequestId());
        String serviceId = serviceData.getServiceId();
        Executor executor = getRequestData();
        ServiceExecutor serviceExecutor = executor.getServiceExecutor();
        EventDispatcher eventDispatcher = executor.getEventDispatcher();
        // 获取服务插件实例
        Service<?> service = getServiceInstance(serviceId);
        // 获取全局上下文并将当前节点的ID作为key放到环境变量中
        FlowExecutionContextImpl flowExecutionContext = getContextBean(FlowExecutionContextImpl.class);
        flowExecutionContext.getVariables().put(getNodeId(), serviceData.getParameters());
        if (flowExecutionContext.isInterrupted()) {
            setIsEnd(true);
        }
        // 构建执行上下文
        ExecutionContext executionContext = LiteFlows.buildExecutionContext(this, flowExecutionContext, serviceData);
        // 执行并处理结果
        eventDispatcher.dispatch(EventHelper.createServiceStartEvent(serviceData, executionContext));
        ExecutionResult<?> executionResult = serviceExecutor.execute(serviceData, service, executionContext);
        // 处理执行结果
        processExecutionResult(flowExecutionContext, executionResult);
        // 最终清理和更新上下文（确保无论成功还是失败都能执行）
        finalizeExecution(flowExecutionContext, executionResult);
        //发布服务结束事件
        eventDispatcher.dispatch(EventHelper.createServiceEndEvent(serviceData, executionContext, executionResult));
    }

    private Service<Object> getServiceInstance(String serviceId) {
        Service<Object> service = Services.getService(serviceId);
        Assert.notNull(service, () -> new RuntimeException(StrUtil.format("cannot found service named '{}'", serviceId)));
        return service;
    }

    private void processExecutionResult(FlowExecutionContextImpl flowExecutionContext, ExecutionResult<?> executionResult) {
        flowExecutionContext.addExecutionResult(executionResult);
        log.debug("Execution completed successfully: {}", executionResult);
    }

    private void finalizeExecution(FlowExecutionContextImpl flowExecutionContext, ExecutionResult<?> executionResult) {
        // 清理和更新循环上下文
        LoopItem currLoopObj = LiteFlows.getLoopObj(this);
        if (Objects.nonNull(currLoopObj)) {
            ExecutionContext loopExecutionContext = flowExecutionContext.getLoopContextMap().get(currLoopObj.getLoopKey());
            if (Objects.nonNull(loopExecutionContext)) {
                executionResult.setLoopId(currLoopObj.getId());
                executionResult.setLoopIndex(currLoopObj.getLoopIndex());
                executionResult.setNrOfInstances(currLoopObj.getNrOfInstances());
                ContextUtils.addResult(loopExecutionContext, executionResult);
            }
        }
    }

}
