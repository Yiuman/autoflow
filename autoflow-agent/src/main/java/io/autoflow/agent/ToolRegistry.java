package io.autoflow.agent;

import dev.langchain4j.agent.tool.ToolSpecification;

import java.util.List;

/**
 * Tool registry interface for mapping human-readable tool names to node IDs (serviceIds).
 */
public interface ToolRegistry {

    List<ToolSpecification> getToolSpecifications();

    String getNodeId(String toolName);

    default void register(String toolName, String nodeId) {
    }

}
