package io.autoflow.agent.tool;

import io.autoflow.agent.ToolRegistry;
import io.autoflow.spi.Service;
import io.autoflow.spi.Services;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of ToolRegistry that wraps the Services registry.
 * Maps human-readable tool names to node IDs (serviceIds).
 */
public class ToolRegistryImpl implements ToolRegistry {

    private final Map<String, String> toolNameToNodeId = new ConcurrentHashMap<>();

    @Override
    public String getNodeId(String toolName) {
        Objects.requireNonNull(toolName, "toolName cannot be null");

        String nodeId = toolNameToNodeId.get(toolName);
        if (nodeId != null) {
            return nodeId;
        }

        return Services.getServiceList().stream()
                .filter(service -> toolName.equals(service.getName()))
                .findFirst()
                .map(Service::getId)
                .orElse(null);
    }

    /**
     * Dynamically registers a tool name to node ID mapping.
     *
     * @param toolName the human-readable tool name
     * @param nodeId   the node ID (serviceId) to associate
     */
    public void register(String toolName, String nodeId) {
        Objects.requireNonNull(toolName, "toolName cannot be null");
        Objects.requireNonNull(nodeId, "nodeId cannot be null");
        toolNameToNodeId.put(toolName, nodeId);
    }
}
