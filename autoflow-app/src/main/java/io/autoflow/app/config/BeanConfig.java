package io.autoflow.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.autoflow.app.service.PropertyDeserializer;
import io.autoflow.spi.model.Property;
import io.ola.crud.serializer.EpochToLocalDateTimeDeserializer;
import io.ola.crud.serializer.LocalDateTimeToEpochSerializer;
import io.ola.crud.utils.JsonbTypeHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;

/**
 * @author yiuman
 * @date 2024/11/22
 */
@Configuration
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
}
