package io.autoflow.core.delegate;

import io.autoflow.core.utils.Flows;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.context.OnceExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.List;
import java.util.Map;

/**
 * 默认的表达式值提解析器（执行上下文值提供器实现）
 *
 * @author yiuman
 * @date 2024/4/2
 */
public class DefaultExpressResolver implements ExpressResolver<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public Object resolve(DelegateExecution execution, String expressionStr) {
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        Map<String, List<ExecutionData>> inputData = (Map<String, List<ExecutionData>>) execution.getTransientVariable(Constants.INPUT_DATA);
        ExecutionContext executionContext = OnceExecutionContext.create(
                Flows.getElementProperties(currentFlowElement),
                inputData
        );
        return new ExecutionContextValueProvider(executionContext).get(expressionStr);
    }
}
