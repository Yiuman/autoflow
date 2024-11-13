package io.autoflow.app.config;

import io.autoflow.app.service.ExecutionInstService;
import io.autoflow.app.service.impl.ServiceExecutorImpl;
import io.autoflow.core.runtime.ServiceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yiuman
 * @date 2024/11/13
 */
@Configuration
public class FlowConfiguration {
    @Bean
    public ServiceExecutor serviceExecutor(ExecutionInstService executionInstService) {
        return new ServiceExecutorImpl(executionInstService);
    }
}
