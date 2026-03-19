package io.autoflow.agent.core.parser;

import io.autoflow.agent.spi.AgentAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonActionParser.
 */
class JsonActionParserTest {

    private JsonActionParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsonActionParser();
    }

    @Test
    void parse_validJsonWithCallToolAction() {
        String json = "{\"action\":\"call_tool\",\"tool\":\"my_tool\",\"args\":{\"key\":\"value\"}}";
        
        AgentAction result = parser.parse(json);
        
        assertEquals("call_tool", result.getAction());
        assertEquals("my_tool", result.getTool());
        assertEquals(Map.of("key", "value"), result.getArgs());
    }

    @Test
    void parse_validJsonWithFinishAction() {
        String json = "{\"action\":\"finish\",\"tool\":null,\"args\":{}}";
        
        AgentAction result = parser.parse(json);
        
        assertEquals("finish", result.getAction());
        assertNull(result.getTool());
        assertTrue(result.getArgs().isEmpty());
    }

    @Test
    void parse_invalidJson_throwsActionParseException() {
        String invalidJson = "not valid json {{{";
        
        ActionParseException exception = assertThrows(ActionParseException.class, () -> parser.parse(invalidJson));
        assertEquals("Failed to parse JSON content", exception.getMessage());
    }

    @Test
    void parse_jsonWithMissingToolField() {
        // When tool field is missing, Jackson will deserialize it as null (no exception)
        String json = "{\"action\":\"call_tool\",\"args\":{}}";
        
        AgentAction result = parser.parse(json);
        
        assertEquals("call_tool", result.getAction());
        assertNull(result.getTool());
        assertTrue(result.getArgs().isEmpty());
    }

    @Test
    void parse_jsonWithEmptyArgs() {
        String json = "{\"action\":\"call_tool\",\"tool\":\"tool_name\",\"args\":{}}";
        
        AgentAction result = parser.parse(json);
        
        assertEquals("call_tool", result.getAction());
        assertEquals("tool_name", result.getTool());
        assertTrue(result.getArgs().isEmpty());
    }

    @Test
    void parse_nullContent_throwsActionParseException() {
        ActionParseException exception = assertThrows(ActionParseException.class, () -> parser.parse(null));
        assertEquals("Content is null or empty", exception.getMessage());
    }

    @Test
    void parse_emptyContent_throwsActionParseException() {
        ActionParseException exception = assertThrows(ActionParseException.class, () -> parser.parse(""));
        assertEquals("Content is null or empty", exception.getMessage());
    }

    @Test
    void parse_blankContent_throwsActionParseException() {
        ActionParseException exception = assertThrows(ActionParseException.class, () -> parser.parse("   "));
        assertEquals("Content is null or empty", exception.getMessage());
    }
}
