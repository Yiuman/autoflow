package io.autoflow.core.runtime;

import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;

import java.time.LocalDateTime;

/**
 * @author yiuman
 * @date 2024/8/2
 */
public final class ServiceExecutors {
    private ServiceExecutors() {
    }

    public static ExecutionResult<ExecutionData> execute(Service service, ExecutionContext executionContext) {
        ExecutionResult<ExecutionData> executionResult = new ExecutionResult<>();
        executionResult.setStartTime(LocalDateTime.now());
        ExecutionData executionData = service.execute(executionContext);
        executionResult.setEndTime(LocalDateTime.now());
        executionResult.setData(executionData);
        return executionResult;
    }

    public static ExecutionResult<ExecutionData> execute(ServiceData serviceData, Service service, ExecutionContext executionContext) {
        ExecutionResult<ExecutionData> executionResult = new ExecutionResult<>();
        executionResult.setFlowId(serviceData.getFlowId());
        executionResult.setNodeId(serviceData.getNodeId());
        executionResult.setServiceId(serviceData.getServiceId());
        executionResult.setStartTime(LocalDateTime.now());
        ExecutionData executionData = service.execute(executionContext);
        executionResult.setEndTime(LocalDateTime.now());
        executionResult.setData(executionData);
        return executionResult;
    }
}
