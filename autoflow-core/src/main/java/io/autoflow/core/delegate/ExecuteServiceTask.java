package io.autoflow.core.delegate;

import cn.hutool.core.collection.CollUtil;
import io.autoflow.core.Services;
import io.autoflow.core.utils.Flows;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import lombok.Data;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.el.FixedValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 执行服务任务
 * 用于通过指定节点名执行具体节点的逻辑
 *
 * @author yiuman
 * @date 2023/7/14
 * @see io.autoflow.core.utils.ServiceNodeConverter
 */
@Data
public class ExecuteServiceTask implements JavaDelegate {

    private FixedValue serviceName;

    @Override
    public void execute(DelegateExecution execution) {
        Service service = Services.getService((String) serviceName.getValue(null));
        FlowExecutionContext flowExecutionContext = FlowExecutionContext.get();
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        flowExecutionContext.getParameters().putAll(
                Flows.getElementProperties(currentFlowElement)
        );
        List<ExecutionData> currentExecutionData = service.execute(flowExecutionContext);
        Map<String, List<ExecutionData>> inputData = flowExecutionContext.getInputData();

        List<ExecutionData> nodeExecutionDataList = Optional
                .ofNullable(inputData.get(currentFlowElement.getName()))
                .orElse(CollUtil.newArrayList());
        nodeExecutionDataList.addAll(currentExecutionData);
        inputData.put(currentFlowElement.getId(), nodeExecutionDataList);
    }
}
