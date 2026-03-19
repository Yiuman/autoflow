package io.autoflow.agent.engine;

import io.autoflow.agent.spi.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of AgentEngine providing ReAct loop orchestration.
 * <p>
 * This is the heart of the agent system - it coordinates the reasoning loop
 * by delegating to specialized components: MemoryStore, Reasoner, ActionParser,
 * NodeExecutor, and ToolRegistry.
 */
@Slf4j
public class DefaultAgentEngine implements AgentEngine {

    private final MemoryStore memoryStore;
    private final Reasoner reasoner;
    private final ActionParser actionParser;
    private final NodeExecutor nodeExecutor;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;

    /**
     * Creates a new DefaultAgentEngine with the specified dependencies.
     *
     * @param memoryStore    store for persisting agent context
     * @param reasoner        LLM streaming inference component
     * @param actionParser    parses LLM output into structured actions
     * @param nodeExecutor    executes tools/nodes with given arguments
     * @param toolRegistry    maps tool names to node IDs
     */
    public DefaultAgentEngine(
            MemoryStore memoryStore,
            Reasoner reasoner,
            ActionParser actionParser,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry) {
        this(memoryStore, reasoner, actionParser, nodeExecutor, toolRegistry, 10);
    }

    /**
     * Creates a new DefaultAgentEngine with the specified dependencies and max steps.
     *
     * @param memoryStore    store for persisting agent context
     * @param reasoner        LLM streaming inference component
     * @param actionParser    parses LLM output into structured actions
     * @param nodeExecutor    executes tools/nodes with given arguments
     * @param toolRegistry    maps tool names to node IDs
     * @param maxSteps        maximum number of reasoning steps before terminating
     */
    public DefaultAgentEngine(
            MemoryStore memoryStore,
            Reasoner reasoner,
            ActionParser actionParser,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            int maxSteps) {
        this.memoryStore = memoryStore;
        this.reasoner = reasoner;
        this.actionParser = actionParser;
        this.nodeExecutor = nodeExecutor;
        this.toolRegistry = toolRegistry;
        this.maxSteps = maxSteps;
    }

    @Override
    public void chat(String sessionId, String input, StreamListener listener) {
        AgentContext context;
        try {
            context = loadOrCreateContext(sessionId, input);
            executeReactLoop(context, listener);
            memoryStore.save(context);
            listener.onComplete();
        } catch (Throwable e) {
            listener.onError(e);
        }
    }

    private AgentContext loadOrCreateContext(String sessionId, String input) {
        AgentContext context = memoryStore.load(sessionId);
        if (context == null) {
            context = new AgentContext(sessionId);
        }
        context.addUserMessage(input);
        return context;
    }

    private void executeReactLoop(AgentContext context, StreamListener listener) {
        for (int i = 0; i < maxSteps; i++) {
            context.incrementStep();
            String output = callReasonerWithStreaming(context, listener);
            AgentAction action = actionParser.parse(output);
            if (action == null || "finish".equals(action.getAction())) {
                break;
            }
            if ("call_tool".equals(action.getAction())) {
                executeTool(context, listener, action);
            }
        }
    }

    private String callReasonerWithStreaming(AgentContext context, StreamListener listener) {
        StringBuilder outputBuilder = new StringBuilder();
        reasoner.think(context, new StreamListener() {
            @Override
            public void onToken(String token) {
                outputBuilder.append(token);
                listener.onToken(token);
            }

            @Override
            public void onToolStart(String toolName) {
                listener.onToolStart(toolName);
            }

            @Override
            public void onToolEnd(String toolName, Object result) {
                listener.onToolEnd(toolName, result);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                listener.onError(e);
            }
        });
        return outputBuilder.toString();
    }

    private void executeTool(AgentContext context, StreamListener listener, AgentAction action) {
        String toolName = action.getTool();
        String nodeId = toolRegistry.getNodeId(toolName);
        listener.onToolStart(toolName);
        Object result = nodeExecutor.execute(nodeId, action.getArgs());
        listener.onToolEnd(toolName, result);
        context.addAssistantMessage("Tool: " + toolName + " Result: " + result);
    }
}
