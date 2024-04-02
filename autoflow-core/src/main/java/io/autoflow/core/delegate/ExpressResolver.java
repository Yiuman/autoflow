package io.autoflow.core.delegate;

import org.flowable.engine.delegate.DelegateExecution;

/**
 * @param <T> 返回的值
 * @author yiuman
 * @date 2024/4/2
 */
public interface ExpressResolver<T> {

    T resolve(DelegateExecution execution, String expressionStr);
}