package io.autoflow.flowable.config;


import io.autoflow.flowable.delegate.DefaultExpressResolver;
import io.autoflow.flowable.delegate.ExecuteServiceListener;
import io.autoflow.flowable.delegate.ExpressResolver;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.boot.ProcessEngineAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/3/29
 */
@Configuration
@AutoConfigureBefore(ProcessEngineAutoConfiguration.class)
@ComponentScan("io.autoflow.flowable.*")
public class FlowableConfiguration {

    @Bean("expressResolver")
    public ExpressResolver<Object> expressResolver() {
        return new DefaultExpressResolver();
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> globalListenerConfigurer() {
        return engineConfiguration -> engineConfiguration.setEventListeners(List.of(new ExecuteServiceListener()));
    }

}

