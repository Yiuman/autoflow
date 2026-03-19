package io.autoflow.agent.spi;

/**
 * Tool registry interface for mapping human-readable tool names to node IDs (serviceIds).
 */
public interface ToolRegistry {

    /**
     * Maps a human-readable tool name to its corresponding node ID (serviceId).
     *
     * @param toolName the human-readable tool name
     * @return the node ID (serviceId) associated with the tool
     */
    String getNodeId(String toolName);
}
