package io.autoflow.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.autoflow.agent.ReActAgent;
import io.autoflow.agent.NodeExecutor;
import io.autoflow.agent.executor.NodeExecutorImpl;
import io.autoflow.agent.memory.InMemoryMemoryStore;
import io.autoflow.agent.ToolRegistry;
import io.autoflow.agent.tool.ToolRegistryImpl;
import io.autoflow.app.service.PropertyDeserializer;
import io.autoflow.spi.model.Property;
import io.ola.crud.serializer.EpochToLocalDateTimeDeserializer;
import io.ola.crud.serializer.LocalDateTimeToEpochSerializer;
import io.ola.crud.utils.JsonbTypeHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author yiuman
 * @date 2024/11/22
 */
@Configuration
@MapperScan(basePackages = {"io.autoflow.app.mapper", "io.autoflow.app.dao"})
public class BeanConfig {
    @Bean
    @ConditionalOnBean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder
                .serializerByType(LocalDateTime.class, new LocalDateTimeToEpochSerializer())
                .deserializerByType(Property.class, new PropertyDeserializer())
                .deserializerByType(LocalDateTime.class, new EpochToLocalDateTimeDeserializer());
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeToEpochSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new EpochToLocalDateTimeDeserializer());
        objectMapper.registerModule(javaTimeModule);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Property.class, new PropertyDeserializer());
        objectMapper.registerModule(module);
        JsonbTypeHandler.setObjectMapper(objectMapper);
        return objectMapper;
    }

    @Bean
    public OpenAiStreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY") != null ? System.getenv("OPENAI_API_KEY") : "dummy")
                .baseUrl(System.getenv("OPENAI_BASE_URL") != null ? System.getenv("OPENAI_BASE_URL") : "https://api.openai.com/v1")
                .modelName(System.getenv("OPENAI_MODEL_NAME") != null ? System.getenv("OPENAI_MODEL_NAME") : "gpt-4o-mini")
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public ReActAgent reActAgent(OpenAiStreamingChatModel chatModel, ToolRegistry toolRegistry, NodeExecutor nodeExecutor) {
        return ReActAgent.builder()
                .chatModel(chatModel)
                .memoryStore(new InMemoryMemoryStore())
                .toolRegistry(toolRegistry)
                .nodeExecutor(nodeExecutor)
                .maxSteps(10)
                .maxToolRetries(3)
                .build();
    }

    @Bean
    public ToolRegistry toolRegistry() {
        return new ToolRegistryImpl();
    }

    @Bean
    public NodeExecutor nodeExecutor() {
        return new NodeExecutorImpl();
    }
}
