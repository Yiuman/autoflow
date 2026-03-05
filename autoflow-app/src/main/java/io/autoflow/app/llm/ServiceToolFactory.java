package io.autoflow.app.llm;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import io.autoflow.spi.Service;
import io.autoflow.spi.Services;
import io.autoflow.spi.model.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Factory for converting SPI Services to LangChain4J ToolSpecification objects.
 * This enables dynamic tool calling by making workflow services available as LLM tools.
 *
 * @author autoflow
 * @date 2025/03/04
 */
public final class ServiceToolFactory {

    private static final Map<String, ToolSpecification> TOOL_SPEC_CACHE = new HashMap<>();

    static {
        refreshToolSpecs();
    }

    private ServiceToolFactory() {
    }

    /**
     * Refreshes the tool specification cache by reloading all SPI services.
     */
    public static void refreshToolSpecs() {
        TOOL_SPEC_CACHE.clear();
        List<Service> serviceList = Services.getServiceList();
        for (Service<?> service : serviceList) {
            ToolSpecification toolSpec = convertToToolSpecification(service);
            TOOL_SPEC_CACHE.put(service.getName(), toolSpec);
        }
    }

    /**
     * Returns all available SPI services as LangChain4J ToolSpecification objects.
     *
     * @return list of all available tool specifications
     */
    public static List<ToolSpecification> getAvailableTools() {
        return List.copyOf(TOOL_SPEC_CACHE.values());
    }

    /**
     * Returns a specific tool by name.
     *
     * @param name the name of the tool/service
     * @return the ToolSpecification for the named tool, or null if not found
     */
    public static ToolSpecification getToolByName(final String name) {
        return TOOL_SPEC_CACHE.get(name);
    }

    /**
     * Converts a SPI Service to a LangChain4J ToolSpecification.
     *
     * @param service the SPI service to convert
     * @return the corresponding ToolSpecification
     */
    public static ToolSpecification convertToToolSpecification(final Service<?> service) {
        JsonObjectSchema.Builder schemaBuilder = JsonObjectSchema.builder();

        List<Property> properties = service.getProperties();
        if (Objects.nonNull(properties)) {
            for (Property property : properties) {
                addPropertyToSchema(schemaBuilder, property);
            }
        }

        return ToolSpecification.builder()
                .name(service.getName())
                .description(buildDescription(service))
                .parameters(schemaBuilder.build())
                .build();
    }

    /**
     * Adds a property to the schema builder based on its type.
     *
     * @param schemaBuilder the schema builder
     * @param property      the property to add
     */
    private static void addPropertyToSchema(
            final JsonObjectSchema.Builder schemaBuilder, final Property property) {
        String propertyName = property.getId();
        String description = buildPropertyDescription(property);
        String type = property.getType();

        if (Objects.isNull(type)) {
            schemaBuilder.addProperty(propertyName, JsonStringSchema.builder()
                    .description(description).build());
            return;
        }

        switch (type.toLowerCase()) {
            case "integer", "int", "long" -> schemaBuilder.addProperty(propertyName,
                    JsonIntegerSchema.builder().description(description).build());
            case "number", "float", "double" -> schemaBuilder.addProperty(propertyName,
                    JsonNumberSchema.builder().description(description).build());
            case "boolean", "bool" -> schemaBuilder.addProperty(propertyName,
                    JsonBooleanSchema.builder().description(description).build());
            case "array", "list" -> schemaBuilder.addProperty(propertyName,
                    JsonArraySchema.builder().items(JsonStringSchema.builder().build())
                            .description(description).build());
            case "object", "map" -> schemaBuilder.addProperty(propertyName,
                    JsonObjectSchema.builder().description(description).build());
            default -> schemaBuilder.addProperty(propertyName,
                    JsonStringSchema.builder().description(description).build());
        }
    }

    /**
     * Builds a description for a property.
     *
     * @param property the property
     * @return the description
     */
    private static String buildPropertyDescription(final Property property) {
        if (Objects.nonNull(property.getDescription())) {
            return property.getDescription();
        }
        if (Objects.nonNull(property.getDisplayName())) {
            return property.getDisplayName();
        }
        return property.getName();
    }

    /**
     * Builds a description for the tool from the service.
     *
     * @param service the service to describe
     * @return the description string
     */
    private static String buildDescription(final Service<?> service) {
        StringBuilder description = new StringBuilder();
        description.append("Executes the ");
        description.append(service.getName());
        description.append(" workflow service");

        List<Property> outputProps = service.getOutputProperties();
        if (Objects.nonNull(outputProps) && !outputProps.isEmpty()) {
            description.append(". Returns: ");
            for (int i = 0; i < outputProps.size(); i++) {
                if (i > 0) {
                    description.append(", ");
                }
                description.append(outputProps.get(i).getName());
            }
        }
        return description.toString();
    }
}
