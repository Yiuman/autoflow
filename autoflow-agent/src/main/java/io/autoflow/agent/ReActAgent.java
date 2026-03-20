package io.autoflow.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final int MAX_TOOL_RETRIES = 3;

    private final MemoryStore memoryStore;
    private final StreamingChatModel chatModel;
    private final NodeExecutor nodeExecutor;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;
    private final PromptTemplateProvider promptProvider;
    private final ObjectMapper objectMapper;
    
    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingTools = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> toolFailureCounts = new ConcurrentHashMap<>();

    public ReActAgent(
            MemoryStore memoryStore,
            StreamingChatModel chatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry) {
        this(memoryStore, chatModel, nodeExecutor, toolRegistry, 10);
    }

    public ReActAgent(
            MemoryStore memoryStore,
            StreamingChatModel chatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            int maxSteps) {
        this(memoryStore, chatModel, nodeExecutor, toolRegistry, 
             new io.autoflow.agent.prompt.DefaultPromptTemplateProvider(), maxSteps);
    }

    public ReActAgent(
            MemoryStore memoryStore,
            StreamingChatModel chatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            PromptTemplateProvider promptProvider,
            int maxSteps) {
        this.memoryStore = memoryStore;
        this.chatModel = chatModel;
        this.nodeExecutor = nodeExecutor;
        this.toolRegistry = toolRegistry;
        this.promptProvider = promptProvider;
        this.maxSteps = maxSteps;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void chat(String sessionId, String input, StreamListener listener) {
        log.info("[Agent] chat session={} input={}", sessionId, input);
        try {
            AgentContext context = loadOrCreateContext(sessionId, input);
            context.setToolSpecifications(toolRegistry.getToolSpecifications());
            toolFailureCounts.clear();
            
            runReactLoop(context, listener);
            
            memoryStore.save(context);
            listener.onComplete();
            log.info("[Agent] chat session={} completed", sessionId);
        } catch (Throwable e) {
            log.error("[Agent] chat session={} error={}", sessionId, e.getMessage());
            listener.onError(e);
        }
    }

    private AgentContext loadOrCreateContext(String sessionId, String input) {
        AgentContext context = memoryStore.load(sessionId);
        if (context == null) {
            context = new AgentContext(sessionId);
            log.info("[Agent] created new context session={}", sessionId);
        }
        context.addUserMessage(input);
        return context;
    }

    private void runReactLoop(AgentContext context, StreamListener listener) {
        for (int step = 0; step < maxSteps; step++) {
            context.incrementStep();
            log.info("[Agent] step={} started", context.getStepCount());

            context.setSystemPrompt(promptProvider.getSystemPromptTemplate());

            LlmResult result = callLlm(context, listener);
            log.info("[Agent] step={} llm_output={}", context.getStepCount(), result.text);

            if (result.toolExecutionRequests().isEmpty()) {
                log.info("[Agent] step={} no tool calls, stopping", context.getStepCount());
                break;
            }
        }
    }

    private LlmResult callLlm(AgentContext context, StreamListener listener) {
        List<ChatMessage> messages = buildMessages(context);
        
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .toolSpecifications(context.getToolSpecifications())
                .build();

        StringBuilder output = new StringBuilder();
        List<ToolExecutionRequest> toolCalls = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        chatModel.chat(request, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String text) {
                output.append(text);
                listener.onToken(text);
            }

            @Override
            public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                ToolExecutionRequest req = completeToolCall.toolExecutionRequest();
                listener.onToolCallComplete(req.name(), req.arguments());
                
                CompletableFuture<Object> future = CompletableFuture.supplyAsync(() ->
                        executeTool(req.name(), req.arguments())
                );
                pendingTools.put(req.name(), future);
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                try {
                    AiMessage ai = response.aiMessage();
                    if (ai != null && ai.hasToolExecutionRequests()) {
                        toolCalls.addAll(ai.toolExecutionRequests());
                    }
                    processToolResults(toolCalls, context);
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

        awaitQuietly(latch);
        
        if (error[0] != null) {
            throw new RuntimeException("LLM call failed", error[0]);
        }

        return new LlmResult(output.toString(), toolCalls);
    }

    private List<ChatMessage> buildMessages(AgentContext context) {
        List<ChatMessage> messages = context.getMessages().stream()
                .map(this::toLangChainMessage)
                .toList();

        String systemPrompt = context.getSystemPrompt();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            List<ChatMessage> withSystem = new ArrayList<>();
            withSystem.add(SystemMessage.from(systemPrompt));
            withSystem.addAll(messages);
            return withSystem;
        }
        return messages;
    }

    private void processToolResults(List<ToolExecutionRequest> toolCalls, AgentContext context) {
        if (pendingTools.isEmpty()) {
            return;
        }

        CompletableFuture.allOf(pendingTools.values().toArray(new CompletableFuture[0])).join();
        List<ToolExecutionRequest> retryList = new ArrayList<>();

        for (ToolExecutionRequest request : toolCalls) {
            String toolName = request.name();
            String failureKey = toolName + ":" + request.arguments();
            
            Object result = getToolResult(toolName, failureKey);

            if (isError(result)) {
                handleToolError(toolName, failureKey, result.toString(), context, retryList, request);
            } else {
                toolFailureCounts.remove(failureKey);
                context.addAssistantMessage("Tool: " + toolName + " Result: " + result);
                log.info("[Agent] tool={} result={}", toolName, result);
            }
        }

        pendingTools.clear();

        if (!retryList.isEmpty()) {
            log.info("[Agent] scheduling {} tool retries", retryList.size());
            retryList.forEach(req -> 
                    context.addAssistantMessage("Retry: " + req.name() + " with arguments: " + req.arguments())
            );
        }
    }

    private Object getToolResult(String toolName, String failureKey) {
        try {
            return pendingTools.get(toolName).get();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return "Error: " + cause.getMessage();
        }
    }

    private boolean isError(Object result) {
        return result.toString().startsWith("Error:");
    }

    private void handleToolError(String toolName, String failureKey, String errorMsg, 
                                  AgentContext context, List<ToolExecutionRequest> retryList,
                                  ToolExecutionRequest request) {
        int failures = toolFailureCounts.getOrDefault(failureKey, 0);
        int remaining = MAX_TOOL_RETRIES - failures;

        if (remaining > 0) {
            toolFailureCounts.put(failureKey, failures + 1);
            String reflection = buildReflectionMessage(toolName, errorMsg, remaining);
            context.addAssistantMessage(reflection);
            retryList.add(request);
            log.info("[Agent] tool={} failed, will retry (remaining={})", toolName, remaining);
        } else {
            context.addAssistantMessage(buildFinalErrorMessage(toolName, errorMsg));
            log.warn("[Agent] tool={} failed after {} retries, giving up", toolName, MAX_TOOL_RETRIES);
        }
    }

    private String buildReflectionMessage(String toolName, String error, int remaining) {
        return """
            Tool Execution Failed: %s
            Error: %s

            Reflection: Analyze what went wrong and plan your next action.
            - Was the tool called with correct arguments?
            - Is there a different tool that could achieve the same goal?
            - Should you try different parameters?

            You have %d retry(s) remaining.
            """.formatted(toolName, error, remaining);
    }

    private String buildFinalErrorMessage(String toolName, String error) {
        return """
            Tool Execution Failed: %s
            Error: %s

            This tool has failed after maximum retries.
            Please provide your best effort answer or explain that the task could not be completed.
            """.formatted(toolName, error);
    }

    private Object executeTool(String toolName, String argsJson) {
        try {
            String nodeId = toolRegistry.getNodeId(toolName);
            Map<String, Object> args = parseArgs(argsJson);
            return nodeExecutor.execute(nodeId, args);
        } catch (Exception e) {
            log.error("[Agent] tool={} execution error={}", toolName, e.getMessage());
            throw e;
        }
    }

    private Map<String, Object> parseArgs(String argsJson) {
        if (argsJson == null || argsJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(argsJson, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("[Agent] failed to parse args JSON={}", argsJson);
            return Map.of();
        }
    }

    private void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
    }

    private ChatMessage toLangChainMessage(io.autoflow.spi.model.ChatMessage msg) {
        return switch (msg.getType()) {
            case USER -> UserMessage.from(msg.getContent());
            case ASSISTANT -> AiMessage.from(msg.getContent());
            default -> SystemMessage.from(msg.getContent());
        };
    }

    public record LlmResult(String text, List<ToolExecutionRequest> toolExecutionRequests) {
    }
}
