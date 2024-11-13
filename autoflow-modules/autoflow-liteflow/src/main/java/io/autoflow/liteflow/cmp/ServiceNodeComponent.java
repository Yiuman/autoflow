package io.autoflow.liteflow.cmp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.core.Services;
import io.autoflow.core.runtime.ServiceExecutor;
import io.autoflow.plugin.loopeachitem.LoopItem;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.*;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@SuppressWarnings("unchecked")
@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceNodeComponent extends NodeComponent {

    private final ServiceExecutor serviceExecutor;

    @Override
    public void process() {
        // 防止转义异常
        ServiceData serviceData = getCmpData(ServiceData.class);
        Assert.notNull(serviceData);
        String serviceId = serviceData.getServiceId();
        // 获取服务插件实例
        Service<Object> service = getServiceInstance(serviceId);
        // 获取全局上下文并将当前节点的ID作为key放到环境变量中
        FlowExecutionContextImpl flowExecutionContext = getContextBean(FlowExecutionContextImpl.class);
        flowExecutionContext.getVariables().put(getNodeId(), serviceData.getParameters());
        // 构建执行上下文
        ExecutionContext executionContext = buildExecutionContext(flowExecutionContext, serviceData);
        // 执行并处理结果
        ExecutionResult<Object> executionResult = executeService(serviceData, service, executionContext);
        // 处理执行结果
        processExecutionResult(flowExecutionContext, executionResult);
        // 最终清理和更新上下文（确保无论成功还是失败都能执行）
        finalizeExecution(flowExecutionContext, executionResult);
    }

    private ExecutionResult<Object> executeService(ServiceData serviceData, Service<Object> service, ExecutionContext executionContext) {
        ExecutionResult<Object> executionResult = serviceExecutor.execute(serviceData, service, executionContext);
        LoopItem currLoopObj = getCurrLoopObj();
        if (Objects.nonNull(currLoopObj)
                && Objects.nonNull(executionContext)
                && executionContext instanceof LoopExecutionContext) {
            executionResult.setLoopId(currLoopObj.getId());
            executionResult.setLoopCounter(currLoopObj.getLoopCounter());
            executionResult.setNrOfInstances(currLoopObj.getNrOfInstances());
            ContextUtils.addResult(executionContext, executionResult);
        }
        return executionResult;
    }

    private Service<Object> getServiceInstance(String serviceId) {
        Service<Object> service = Services.getService(serviceId);
        Assert.notNull(service, () -> new RuntimeException(StrUtil.format("cannot found service named '{}'", serviceId)));
        return service;
    }

    private ExecutionContext buildExecutionContext(FlowExecutionContextImpl flowExecutionContext,
                                                   ServiceData serviceData) {
        ExecutionContext onceExecutionContext;
        LoopItem currLoopObj = getCurrLoopObj();

        if (Objects.nonNull(currLoopObj)) {
            onceExecutionContext = getLoopExecutionContext(flowExecutionContext, currLoopObj);
        } else {
            onceExecutionContext = new OnceExecutionContext(flowExecutionContext, serviceData.getParameters());
        }

        return onceExecutionContext;
    }

    private ExecutionContext getLoopExecutionContext(FlowExecutionContextImpl flowExecutionContext, LoopItem currLoopObj) {
        Map<String, ExecutionContext> loopContextMap = flowExecutionContext.getLoopContextMap();
        ExecutionContext loopExecutionContext = Optional.ofNullable(loopContextMap.get(currLoopObj.getLoopKey()))
                .orElseGet(() -> createNewLoopContext(loopContextMap, currLoopObj));

        OnceExecutionContext onceExecutionContext = new OnceExecutionContext(loopExecutionContext);
        onceExecutionContext.getParameters().putAll(getCmpData(ServiceData.class).getParameters());
        onceExecutionContext.getVariables().putAll(BeanUtil.beanToMap(currLoopObj));

        return onceExecutionContext;
    }

    private ExecutionContext createNewLoopContext(Map<String, ExecutionContext> loopContextMap, LoopItem currLoopObj) {
        ExecutionContext loopContext;
        LoopItem preLoop = currLoopObj.getPreLoop();
        if (Objects.isNull(preLoop)) {
            ExecutionContext snapshoot = loopContextMap.get(currLoopObj.getId());
            if (Objects.isNull(snapshoot)) {
                snapshoot = new LoopExecutionContext(getContextBean(FlowExecutionContextImpl.class));
                loopContextMap.put(currLoopObj.getId(), snapshoot);
            }
            loopContext = new LoopExecutionContext(snapshoot);
        } else {
            loopContext = new LoopExecutionContext(loopContextMap.get(preLoop.getLoopKey()));
        }
        loopContextMap.put(currLoopObj.getLoopKey(), loopContext);
        return loopContext;
    }

    private void processExecutionResult(FlowExecutionContextImpl flowExecutionContext, ExecutionResult<Object> executionResult) {
        flowExecutionContext.addExecutionResult(executionResult);
        log.debug("Execution completed successfully: {}", executionResult);
    }

    private void finalizeExecution(FlowExecutionContextImpl flowExecutionContext, ExecutionResult<Object> executionResult) {
        // 清理和更新循环上下文
        LoopItem currLoopObj = getCurrLoopObj();
        if (Objects.nonNull(currLoopObj)) {
            ExecutionContext loopExecutionContext = flowExecutionContext.getLoopContextMap().get(currLoopObj.getLoopKey());
            if (Objects.nonNull(loopExecutionContext)) {
                executionResult.setLoopId(currLoopObj.getId());
                executionResult.setLoopCounter(currLoopObj.getLoopCounter());
                executionResult.setNrOfInstances(currLoopObj.getNrOfInstances());
                ContextUtils.addResult(loopExecutionContext, executionResult);
            }
        }
    }

}
