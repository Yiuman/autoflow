package io.autoflow.agent;

import dev.langchain4j.agent.tool.ToolSpecification;
import java.util.List;

/**
 * Interface for LLM streaming inference.
 * Implementations handle token streaming via the listener.
 */
public interface Reasoner {

    void think(AgentContext context, StreamListener listener);

    default void think(AgentContext context, StreamListener listener,
                       List<ToolSpecification> toolSpecifications) {
        // Default implementation ignores tools - for backward compatibility
        think(context, listener);
    }
}
