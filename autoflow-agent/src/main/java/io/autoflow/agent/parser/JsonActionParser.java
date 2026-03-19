package io.autoflow.agent.core.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.autoflow.agent.spi.ActionParser;
import io.autoflow.agent.spi.AgentAction;

public class JsonActionParser implements ActionParser {

    private final ObjectMapper objectMapper;

    public JsonActionParser() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public AgentAction parse(String content) {
        if (content == null || content.isBlank()) {
            throw new ActionParseException("Content is null or empty");
        }
        try {
            return objectMapper.readValue(content, AgentAction.class);
        } catch (JsonProcessingException e) {
            throw new ActionParseException("Failed to parse JSON content", e);
        }
    }
}
