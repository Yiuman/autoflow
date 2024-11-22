package io.autoflow.app.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.model.SimpleProperty;

import java.io.IOException;

/**
 * @author yiuman
 * @date 2024/11/22
 */
public class PropertyDeserializer extends JsonDeserializer<Property> {
    @Override
    public Property deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        return mapper.treeToValue(node, SimpleProperty.class);
    }
}
