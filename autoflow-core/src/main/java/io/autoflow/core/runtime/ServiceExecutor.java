package io.autoflow.core.runtime;

import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;

/**
 * @author yiuman
 * @date 2024/11/13
 */
public interface ServiceExecutor {
    <T> ExecutionResult<T> execute(ServiceData serviceData, Service<T> service, ExecutionContext executionContext);
}