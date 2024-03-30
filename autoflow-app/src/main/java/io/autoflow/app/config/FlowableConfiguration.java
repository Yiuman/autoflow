package io.autoflow.app.config;

import io.autoflow.app.flowable.ExecuteServiceListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/3/29
 */
@Configuration
public class FlowableConfiguration {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> globalListenerConfigurer() {
        return engineConfiguration -> engineConfiguration.setEventListeners(List.of(new ExecuteServiceListener()));

    }

}
