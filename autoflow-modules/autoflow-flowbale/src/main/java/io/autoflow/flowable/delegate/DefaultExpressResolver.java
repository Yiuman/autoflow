package io.autoflow.flowable.delegate;

import io.autoflow.spi.context.FlowContextHolder;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import org.flowable.engine.delegate.DelegateExecution;

/**
 * 默认的表达式值提解析器（执行上下文值提供器实现）
 *
 * @author yiuman
 * @date 2024/4/2
 */
public class DefaultExpressResolver implements ExpressResolver<Object> {

    @Override
    public Object resolve(DelegateExecution execution, String expressionStr) {
        return new ExecutionContextValueProvider(FlowContextHolder.get()).get(expressionStr);
    }
}
