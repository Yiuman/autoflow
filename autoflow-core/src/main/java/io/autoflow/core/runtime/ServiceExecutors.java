package io.autoflow.core.runtime;

import cn.hutool.core.util.StrUtil;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDateTime;

/**
 * @author yiuman
 * @date 2024/8/2
 */
public final class ServiceExecutors {
    private static final Log LOGGER = LogFactory.getLog(ServiceExecutors.class);

    private ServiceExecutors() {
    }

    public static <T> ExecutionResult<T> execute(Service<T> service, ExecutionContext executionContext) {
        ExecutionResult<T> executionResult = new ExecutionResult<>();
        executionResult.setStartTime(LocalDateTime.now());
        T executionData = service.execute(executionContext);
        executionResult.setEndTime(LocalDateTime.now());
        executionResult.setData(executionData);
        return executionResult;
    }

    public static ServiceExecutor getDefaultServiceExecutor() {
        return DefaultServiceExecutor.Instance.DEFAULT_SERVICE_EXECUTOR;
    }

    public static <T> ExecutionResult<T> execute(ServiceData serviceData, Service<T> service, ExecutionContext executionContext) {
        ExecutionResult<T> executionResult = new ExecutionResult<>();
        executionResult.setFlowId(serviceData.getFlowId());
        executionResult.setFlowInstId(serviceData.getFlowInstId());
        executionResult.setNodeId(serviceData.getNodeId());
        executionResult.setServiceId(serviceData.getServiceId());
        executionResult.setStartTime(LocalDateTime.now());
        T executionData = service.execute(executionContext);
        executionResult.setEndTime(LocalDateTime.now());
        executionResult.setData(executionData);
        return executionResult;
    }

    public static class DefaultServiceExecutor implements ServiceExecutor {
        @Override
        public <T> ExecutionResult<T> execute(ServiceData serviceData, Service<T> service, ExecutionContext executionContext) {
            ExecutionResult<T> executionResult;
            try {
                executionResult = ServiceExecutors.execute(serviceData, service, executionContext);
            } catch (Throwable throwable) {
                LOGGER.debug(StrUtil.format("'{}' node execute error", serviceData.getServiceId()), throwable);
                executionResult = ExecutionResult.error(serviceData, throwable);
            }
            return executionResult;
        }

        public static class Instance {
            public static final DefaultServiceExecutor DEFAULT_SERVICE_EXECUTOR = new DefaultServiceExecutor();
        }
    }
}
