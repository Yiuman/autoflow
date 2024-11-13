package io.autoflow.liteflow.config;

import io.autoflow.core.runtime.ServiceExecutor;
import io.autoflow.core.runtime.ServiceExecutors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author yiuman
 * @date 2024/4/12
 */
@Configuration
@ComponentScan("io.autoflow.liteflow.*")
public class LiteFlowAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public ServiceExecutor serviceExecutor() {
        return ServiceExecutors.getDefaultServiceExecutor();
    }
}
