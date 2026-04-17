package io.autoflow.agent.tool;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import io.autoflow.spi.Service;
import io.autoflow.spi.model.Property;
import java.util.List;

public class ToolSpecificationConverter {

    public static List<ToolSpecification> convert(Service<?> service) {
        if (service == null) {
            return List.of();
        }
        
        String description = service.getDescription() + " [serviceId: " + service.getId() + "]";
        ToolSpecification spec = ToolSpecification.builder()
            .name(service.getName())
            .description(description)
            .parameters(convertParameters(service.getProperties()))
            .build();
        
        return List.of(spec);
    }
    
    private static JsonObjectSchema convertParameters(List<Property> properties) {
        if (properties == null || properties.isEmpty()) {
            return JsonObjectSchema.builder().build();
        }
        
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();
        
        for (Property prop : properties) {
            String type = prop.getType() != null ? prop.getType() : "string";
            JsonSchemaElement propSchema = createSchemaElement(type, prop.getDescription());
            builder.addProperty(prop.getName(), propSchema);
        }
        
        return builder.build();
    }
    
    private static JsonSchemaElement createSchemaElement(String type, String description) {
        String lowerType = type.toLowerCase();
        switch (lowerType) {
            case "number":
                return JsonNumberSchema.builder().description(description).build();
            case "integer":
                return JsonIntegerSchema.builder().description(description).build();
            case "boolean":
                return JsonBooleanSchema.builder().description(description).build();
            case "array":
                return JsonArraySchema.builder().description(description).build();
            default:
                return JsonStringSchema.builder().description(description).build();
        }
    }
}