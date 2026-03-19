package io.autoflow.agent.tool;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.Property;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ToolSpecificationConverterTest {

    @Test
    void shouldConvertServiceToToolSpecification() {
        Service<?> service = new TestService();
        
        List<ToolSpecification> specs = ToolSpecificationConverter.convert(service);
        
        assertEquals(1, specs.size());
        ToolSpecification spec = specs.get(0);
        assertEquals("calculator", spec.name());
        assertNotNull(spec.parameters());
    }
    
    @Test
    void shouldReturnEmptyListWhenServiceIsNull() {
        List<ToolSpecification> specs = ToolSpecificationConverter.convert(null);
        assertEquals(0, specs.size());
    }
    
    @Test
    void shouldConvertPropertiesToJsonSchema() {
        Service<?> service = new TestService();
        
        List<ToolSpecification> specs = ToolSpecificationConverter.convert(service);
        
        ToolSpecification spec = specs.get(0);
        JsonObjectSchema params = spec.parameters();
        assertNotNull(params);
        assertNotNull(params.properties());
        assertEquals(2, params.properties().size());
    }
    
    static class TestService implements Service<String> {
        public String getName() { return "calculator"; }
        public List<Property> getProperties() {
            return List.of(new TestProperty("a", "number", "第一个数"),
                          new TestProperty("b", "number", "第二个数"));
        }
        public List<Property> getOutputProperties() { return List.of(); }
        public String execute(ExecutionContext ctx) { return "4"; }
    }
    
    static class TestProperty implements Property {
        private final String name;
        private final String type;
        private final String description;
        
        TestProperty(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getId() { return name; }
        public String getDisplayName() { return name; }
        public Object getDefaultValue() { return null; }
    }
}