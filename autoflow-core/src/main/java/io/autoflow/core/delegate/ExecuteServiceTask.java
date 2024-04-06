package io.autoflow.core.delegate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import io.autoflow.core.Services;
import io.autoflow.core.utils.Flows;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.el.FixedValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 执行服务任务
 * 用于通过指定节点名执行具体节点的逻辑
 *
 * @author yiuman
 * @date 2023/7/14
 * @see io.autoflow.core.utils.ServiceNodeConverter
 */
@Data
@Slf4j
public class ExecuteServiceTask implements JavaDelegate {

    private FixedValue serviceId;

    @Override
    public void execute(DelegateExecution execution) {
        String serviceIdValue = (String) serviceId.getValue(null);
        StopWatch stopWatch = new StopWatch(StrUtil.format("【{} Task】", serviceIdValue));
        stopWatch.start();
        Service service = Services.getService(serviceIdValue);
        Assert.notNull(service, () -> new RuntimeException(StrUtil.format("cannot found service named '{}'", serviceIdValue)));
        FlowExecutionContext flowExecutionContext = FlowExecutionContext.get();
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        flowExecutionContext.getParameters().putAll(execution.getVariables());
        flowExecutionContext.getParameters().putAll(Flows.getElementProperties(currentFlowElement));
        ExecutionData currentExecutionData;
        try {
            //添加瞬态变量（不会序列化保存，只作用与当前的流程流转相关）
            execution.setTransientVariablesLocal(flowExecutionContext.getParameters());
            execution.setTransientVariableLocal(Constants.INPUT_DATA, flowExecutionContext.getInputData());
            currentExecutionData = service.execute(flowExecutionContext);
        } catch (Throwable throwable) {
            log.error(StrUtil.format("'{}' node execute error", serviceIdValue), throwable);
            currentExecutionData = ExecutionData.error(serviceIdValue, throwable);
        } finally {
            flowExecutionContext.getParameters().clear();
            execution.removeTransientVariables();
        }

        Map<String, List<ExecutionData>> inputData = flowExecutionContext.getInputData();

        List<ExecutionData> nodeExecutionDataList = Optional
                .ofNullable(inputData.get(currentFlowElement.getId()))
                .orElse(CollUtil.newArrayList());
        nodeExecutionDataList.add(currentExecutionData);
        inputData.put(currentFlowElement.getId(), nodeExecutionDataList);
        stopWatch.stop();
        log.debug("\n" + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}
