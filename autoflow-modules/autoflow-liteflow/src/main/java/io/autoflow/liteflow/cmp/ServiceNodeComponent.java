package io.autoflow.liteflow.cmp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.core.Services;
import io.autoflow.core.runtime.ServiceExecutors;
import io.autoflow.plugin.loopeachitem.LoopItem;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ContextUtils;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.context.OnceExecutionContext;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        stopWatch.start("获取服务插件实例");
        Service<Object> service = Services.getService(serviceId);
        Assert.notNull(service, () -> new RuntimeException(StrUtil.format("cannot found service named '{}'", serviceId)));
        stopWatch.stop();
        stopWatch.start("获取全局上下文");
        FlowExecutionContextImpl flowExecutionContext = getContextBean(FlowExecutionContextImpl.class);
        //把当前的节点的ID作为key 参数作为值放下环境变量中后续其他节点引用
        flowExecutionContext.getVariables().put(getNodeId(), serviceData.getParameters());
        stopWatch.stop();
        ExecutionResult<Object> executionResult = null;
        ExecutionContext loopExecutionContext = null;
        OnceExecutionContext onceExecutionContext;
        LoopItem currLoopObj = getCurrLoopObj();
        try {
            stopWatch.start("构建执行上下文");
            if (Objects.nonNull(currLoopObj)) {
                Map<String, ExecutionContext> loopContextMap = flowExecutionContext.getLoopContextMap();
                loopExecutionContext = Optional.ofNullable(loopContextMap.get(currLoopObj.getLoopKey()))
                        .orElseGet(() -> {
                            ExecutionContext loopContext;
                            LoopItem preLoop = currLoopObj.getPreLoop();
                            if (Objects.isNull(preLoop)) {
                                ExecutionContext snapshoot = loopContextMap.get(currLoopObj.getId());
                                if (Objects.isNull(snapshoot)) {
                                    snapshoot = new OnceExecutionContext(flowExecutionContext);
                                    loopContextMap.put(currLoopObj.getId(), snapshoot);
                                }
                                loopContext = new OnceExecutionContext(snapshoot);
                            } else {
                                loopContext = new OnceExecutionContext(loopContextMap.get(preLoop.getLoopKey()));
                            }
                            loopContextMap.put(currLoopObj.getLoopKey(), loopContext);
                            return loopContext;
                        });
                onceExecutionContext = new OnceExecutionContext(loopExecutionContext);
                onceExecutionContext.getParameters().putAll(serviceData.getParameters());
                onceExecutionContext.getVariables().putAll(BeanUtil.beanToMap(currLoopObj));
            } else {
                onceExecutionContext = new OnceExecutionContext(flowExecutionContext, serviceData.getParameters());
            }
            stopWatch.stop();
            stopWatch.start("执行");
            executionResult = ServiceExecutors.execute(serviceData, service, onceExecutionContext);

        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", serviceId), throwable);
            executionResult = ExecutionResult.error(serviceData, throwable);
        } finally {
            if (Objects.nonNull(currLoopObj) && Objects.nonNull(loopExecutionContext)) {
                executionResult.setLoopId(currLoopObj.getId());
                executionResult.setLoopCounter(currLoopObj.getLoopCounter());
                executionResult.setNrOfInstances(currLoopObj.getNrOfInstances());
                ContextUtils.addResult(loopExecutionContext, executionResult);
            }
            stopWatch.stop();
        }

        stopWatch.start("处理执行结果");
        flowExecutionContext.addExecutionResult(executionResult);
        stopWatch.stop();
        log.debug("\n" + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

}
