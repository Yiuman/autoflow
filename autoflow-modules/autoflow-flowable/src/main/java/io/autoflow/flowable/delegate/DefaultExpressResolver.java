package io.autoflow.flowable.delegate;

import io.autoflow.spi.context.FlowContextHolder;
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
        return FlowContextHolder.get().parseValue(expressionStr);
    }
}
