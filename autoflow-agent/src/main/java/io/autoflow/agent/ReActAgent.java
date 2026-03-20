package io.autoflow.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.autoflow.agent.prompt.DefaultPromptTemplateProvider;
import io.autoflow.agent.prompt.PromptTemplateProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ReActAgent implements AgentEngine {

    private final MemoryStore memoryStore;
    private final StreamingChatModel streamingChatModel;
    private final NodeExecutor nodeExecutor;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;
    private final PromptTemplateProvider promptTemplateProvider;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CompletableFuture<Object>> toolFutures = new ConcurrentHashMap<>();

    public ReActAgent(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry) {
        this(memoryStore, streamingChatModel, nodeExecutor, toolRegistry, new DefaultPromptTemplateProvider());
    }

    public ReActAgent(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            int maxSteps) {
        this(memoryStore, streamingChatModel, nodeExecutor, toolRegistry,
             new DefaultPromptTemplateProvider(), maxSteps);
    }

    public ReActAgent(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            PromptTemplateProvider promptTemplateProvider) {
        this(memoryStore, streamingChatModel, nodeExecutor, toolRegistry, 
             promptTemplateProvider, 10);
    }

    public ReActAgent(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            PromptTemplateProvider promptTemplateProvider,
            int maxSteps) {
        this.memoryStore = memoryStore;
        this.streamingChatModel = streamingChatModel;
        this.nodeExecutor = nodeExecutor;
        this.toolRegistry = toolRegistry;
        this.promptTemplateProvider = promptTemplateProvider;
        this.maxSteps = maxSteps;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void chat(String sessionId, String input, StreamListener listener) {
        log.info("[Agent] Session: {}, Input: {}", sessionId, input);
        AgentContext context;
        try {
            context = loadOrCreateContext(sessionId, input);
            context.setToolSpecifications(toolRegistry.getToolSpecifications());
            executeReactLoop(context, listener);
            memoryStore.save(context);
            listener.onComplete();
            log.info("[Agent] Session: {} completed", sessionId);
        } catch (Throwable e) {
            log.error("[Agent] Session: {} error: {}", sessionId, e.getMessage());
            listener.onError(e);
        }
    }

    private AgentContext loadOrCreateContext(String sessionId, String input) {
        AgentContext context = memoryStore.load(sessionId);
        if (context == null) {
            context = new AgentContext(sessionId);
            log.info("[Agent] Created new context for session: {}", sessionId);
        }
        context.addUserMessage(input);
        return context;
    }

    private void executeReactLoop(AgentContext context, StreamListener listener) {
        for (int i = 0; i < maxSteps; i++) {
            context.incrementStep();
            int currentStep = context.getStepCount();
            log.info("[Agent] Step {} started", currentStep);

            // Set system prompt from provider
            if (promptTemplateProvider != null) {
                context.setSystemPrompt(promptTemplateProvider.getSystemPromptTemplate());
            }


            // Tools are executed asynchronously in callLlmWithStreaming
            LlmResult result = callLlmWithStreaming(context, listener);
            log.info("[Agent] LLM output: {}", result.text);

            // Only check if there are tool calls, don't execute them
            if (result.toolExecutionRequests == null || result.toolExecutionRequests.isEmpty()) {
                log.info("[Agent] No more tool calls, stopping loop");
                break;
            }
        }
    }

    private LlmResult callLlmWithStreaming(AgentContext context, StreamListener listener) {
        List<ChatMessage> messages = context.getMessages().stream()
                .map(this::toLangChainMessage)
                .toList();

        if (context.getSystemPrompt() != null && !context.getSystemPrompt().isEmpty()) {
            messages = new ArrayList<>(messages);
            messages.add(0, SystemMessage.from(context.getSystemPrompt()));
        }

        List<ToolSpecification> tools = context.getToolSpecifications();
        ChatRequest.Builder requestBuilder = ChatRequest.builder().messages(messages);
        if (tools != null && !tools.isEmpty()) {
            requestBuilder.toolSpecifications(tools);
        }
        ChatRequest request = requestBuilder.build();

        StringBuilder textBuilder = new StringBuilder();
        List<ToolExecutionRequest> toolRequests = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        streamingChatModel.chat(request, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                textBuilder.append(partialResponse);
                listener.onToken(partialResponse);
            }

            @Override
            public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                ToolExecutionRequest request = completeToolCall.toolExecutionRequest();
                String toolName = request.name();
                String argsJson = request.arguments();
                listener.onToolCallComplete(toolName, argsJson);
                CompletableFuture<Object> future = CompletableFuture.supplyAsync(() ->
                    executeToolAsync(toolName, argsJson, listener)
                );
                toolFutures.put(toolName, future);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                try {
                    AiMessage aiMessage = completeResponse.aiMessage();
                    if (aiMessage != null && aiMessage.hasToolExecutionRequests()) {
                        toolRequests.addAll(aiMessage.toolExecutionRequests());
                    }
                    collectToolResults(toolRequests, context, error);
                } catch (Throwable t) {
                    error[0] = t;
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onError(Throwable e) {
                error[0] = e;
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("LLM call interrupted", e);
        }

        if (error[0] != null) {
            throw new RuntimeException("LLM call failed", error[0]);
        }

        return new LlmResult(textBuilder.toString(), toolRequests);
    }

    private Object executeToolAsync(String toolName, String argsJson, StreamListener listener) {
        try {
            String nodeId = toolRegistry.getNodeId(toolName);
            Map<String, Object> args = parseArgs(argsJson);
            listener.onToolStart(toolName);
            Object result = nodeExecutor.execute(nodeId, args);
            listener.onToolEnd(toolName, result);
            return result;
        } catch (Exception e) {
            log.error("[Agent] Tool {} execution failed: {}", toolName, e.getMessage());
            throw e;
        }
    }

    private void collectToolResults(List<ToolExecutionRequest> toolRequests, AgentContext context, Throwable[] error) {
        if (toolFutures.isEmpty()) {
            return;
        }
        CompletableFuture.allOf(toolFutures.values().toArray(new CompletableFuture[0])).join();
        for (ToolExecutionRequest request : toolRequests) {
            String toolName = request.name();
            Object result;
            try {
                result = toolFutures.get(toolName).get();
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                result = "Error: " + cause.getMessage();
                log.error("[Agent] Tool {} execution failed: {}", toolName, cause.getMessage());
                error[0] = cause;
                return;
            }
            context.addAssistantMessage("Tool: " + toolName + " Result: " + result);
            log.info("[Agent] Tool {} result added to context: {}", toolName, result);
        }
    }

    private void executeTool(AgentContext context, StreamListener listener, String toolName, String argsJson) {
        String nodeId = toolRegistry.getNodeId(toolName);
        log.info("[Agent] Executing tool: {} -> nodeId: {}", toolName, nodeId);
        Map<String, Object> args = parseArgs(argsJson);
        listener.onToolStart(toolName);
        Object result = nodeExecutor.execute(nodeId, args);
        log.info("[Agent] Tool result: {}", result);
        listener.onToolEnd(toolName, result);
        context.addAssistantMessage("Tool: " + toolName + " Result: " + result);
    }

    private Map<String, Object> parseArgs(String argsJson) {
        if (argsJson == null || argsJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(argsJson, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("[Agent] Failed to parse tool args as JSON: {}", argsJson);
            return Map.of();
        }
    }

    private ChatMessage toLangChainMessage(io.autoflow.spi.model.ChatMessage chatMessage) {
        return switch (chatMessage.getType()) {
            case USER -> UserMessage.from(chatMessage.getContent());
            case ASSISTANT -> AiMessage.from(chatMessage.getContent());
            default -> SystemMessage.from(chatMessage.getContent());
        };
    }

    private static class LlmResult {
        final String text;
        final List<dev.langchain4j.agent.tool.ToolExecutionRequest> toolExecutionRequests;

        LlmResult(String text, List<dev.langchain4j.agent.tool.ToolExecutionRequest> toolExecutionRequests) {
            this.text = text;
            this.toolExecutionRequests = toolExecutionRequests;
        }
    }
}
