package io.autoflow.agent.spi;

import java.util.Map;

/**
 * Node executor for running tools/nodes with given arguments.
 *
 * @author yiuman
 * @date 2023/7/11
 */
public interface NodeExecutor {

    /**
     * Execute a tool/node with given arguments.
     *
     * @param nodeId the identifier of the node to execute
     * @param args   the execution arguments
     * @return the execution result
     */
    Object execute(String nodeId, Map<String, Object> args);

}
