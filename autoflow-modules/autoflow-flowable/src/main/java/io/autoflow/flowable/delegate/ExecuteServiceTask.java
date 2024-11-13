package io.autoflow.flowable.delegate;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import io.autoflow.core.Services;
import io.autoflow.core.runtime.ServiceExecutors;
import io.autoflow.flowable.utils.Flows;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.FlowContextHolder;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.el.FixedValue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 执行服务任务
 * 用于通过指定节点名执行具体节点的逻辑
 *
 * @author yiuman
 * @date 2023/7/14
 * @see io.autoflow.flowable.utils.ServiceNodeConverter
 */
@SuppressWarnings("unchecked")
@Data
@Slf4j
public class ExecuteServiceTask implements JavaDelegate {

    private FixedValue serviceId;

    @Override
    public void execute(DelegateExecution execution) {
        String serviceIdValue = (String) serviceId.getValue(null);
        StopWatch stopWatch = new StopWatch(StrUtil.format("【{} Task】", serviceIdValue));
        stopWatch.start();
        Service<Object> service = Services.getService(serviceIdValue);
        Assert.notNull(service, () -> new RuntimeException(StrUtil.format("cannot found service named '{}'", serviceIdValue)));

        FlowExecutionContextImpl flowExecutionContext = (FlowExecutionContextImpl) FlowContextHolder.get();
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        if (currentFlowElement instanceof Activity activity) {
            MultiInstanceLoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
            if (Objects.nonNull(loopCharacteristics)) {
                flowExecutionContext.getVariables().put(
                        "elementVariable",
                        execution.getVariables().get(loopCharacteristics.getElementVariable())
                );
            }
        }

        flowExecutionContext.getVariables().putAll(execution.getVariables());
        flowExecutionContext.getParameters().putAll(Flows.getElementProperties(currentFlowElement));
        ExecutionResult<Object> executionResult;
        ServiceData serviceData = new ServiceData();
        serviceData.setFlowId(execution.getProcessInstanceId());
        serviceData.setNodeId(execution.getCurrentActivityId());
        serviceData.setServiceId(serviceIdValue);
        serviceData.setParameters(execution.getVariables());
        try {
            //添加瞬态变量（不会序列化保存，只作用与当前的流程流转相关）
            execution.setTransientVariablesLocal(flowExecutionContext.getParameters());
            execution.setTransientVariableLocal(Constants.INPUT_DATA, flowExecutionContext.getInputData());
            executionResult = ServiceExecutors.execute(serviceData, service, flowExecutionContext);
        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", serviceIdValue), throwable);
            executionResult = ExecutionResult.error(serviceData, throwable);
        } finally {
            flowExecutionContext.getParameters().clear();
            execution.removeTransientVariables();
        }

        flowExecutionContext.addExecutionResult(executionResult);
        stopWatch.stop();
        log.debug("\n" + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}

