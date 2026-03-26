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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public final class ReActAgent implements AgentEngine {

    private final StreamingChatModel chatModel;
    private final NodeExecutor nodeExecutor;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;
    private final PromptTemplateProvider promptProvider;
    private final ObjectMapper objectMapper;
    private final ToolRetryHandler retryHandler;

    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingTools = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> toolIdToName = new ConcurrentHashMap<>();
    private final StringBuilder thinkingBuffer = new StringBuilder();

    private ReActAgent(Builder builder) {
        this.chatModel = builder.chatModel;
        this.nodeExecutor = builder.nodeExecutor;
        this.toolRegistry = builder.toolRegistry;
        this.promptProvider = builder.promptProvider;
        this.maxSteps = builder.maxSteps;
        this.objectMapper = new ObjectMapper();
        this.retryHandler = new ToolRetryHandler(builder.maxToolRetries);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private StreamingChatModel chatModel;
        private NodeExecutor nodeExecutor;
        private ToolRegistry toolRegistry;
        private PromptTemplateProvider promptProvider = new io.autoflow.agent.prompt.DefaultPromptTemplateProvider();
        private int maxSteps = 10;
        private int maxToolRetries = 3;

        public Builder chatModel(StreamingChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder nodeExecutor(NodeExecutor nodeExecutor) {
            this.nodeExecutor = nodeExecutor;
            return this;
        }

        public Builder toolRegistry(ToolRegistry toolRegistry) {
            this.toolRegistry = toolRegistry;
            return this;
        }

        public Builder promptProvider(PromptTemplateProvider promptProvider) {
            this.promptProvider = promptProvider;
            return this;
        }

        public Builder maxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        public Builder maxToolRetries(int maxToolRetries) {
            this.maxToolRetries = maxToolRetries;
            return this;
        }

        public ReActAgent build() {
            return new ReActAgent(this);
        }
    }

    @Override
    public void chat(io.autoflow.agent.ChatRequest request, StreamListener listener) {
        chat(request, chatModel, listener);
    }

    public void chat(io.autoflow.agent.ChatRequest request, StreamingChatModel model, StreamListener listener) {
        log.info("[Agent] chat input={}", request.getInput());
        StringBuilder fullOutput = new StringBuilder();
        try {
            AgentContext context = AgentContext.from(request);
            context.setToolSpecifications(toolRegistry.getToolSpecifications());
            if (context.getSystemPrompt() == null || context.getSystemPrompt().isBlank()) {
                context.setSystemPrompt(promptProvider.getSystemPromptTemplate());
            }
            retryHandler.clear();

            runReactLoop(context, model, listener, fullOutput);

            listener.onComplete(fullOutput.toString());
            log.info("[Agent] chat completed");
        } catch (Throwable e) {
            log.error("[Agent] chat error", e);
            listener.onError(e);
        }
    }

    private void runReactLoop(AgentContext context, StreamingChatModel model, StreamListener listener, StringBuilder fullOutput) {
        for (int step = 0; step < maxSteps; step++) {
            context.incrementStep();
            log.info("[Agent] step={} started", context.getStepCount());

            LlmResult result = callLlm(context, model, listener, fullOutput);
            log.info("[Agent] step={} llm_output={}", context.getStepCount(), result.text);

            listener.onRoundComplete();

            if (result.toolExecutionRequests().isEmpty()) {
                log.info("[Agent] step={} no tool calls, stopping", context.getStepCount());
                break;
            }
        }
    }

    private LlmResult callLlm(AgentContext context, StreamingChatModel model, StreamListener listener, StringBuilder fullOutput) {
        List<ChatMessage> messages = buildMessages(context);

        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .toolSpecifications(context.getToolSpecifications())
                .build();

        StringBuilder output = new StringBuilder();
        List<ToolExecutionRequest> toolCalls = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        model.chat(request, createResponseHandler(
                output, fullOutput, toolCalls, context, listener, error, latch));

        awaitQuietly(latch);

        if (error.get() != null) {
            throw new RuntimeException("LLM call failed", error.get());
        }

        return new LlmResult(output.toString(), toolCalls);
    }

    private StreamingChatResponseHandler createResponseHandler(
            StringBuilder output, StringBuilder fullOutput,
            List<ToolExecutionRequest> toolCalls, AgentContext context,
            StreamListener listener, AtomicReference<Throwable> error, CountDownLatch latch) {
        return new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String text) {
                handlePartialResponse(text, output, fullOutput, listener);
            }

            @Override
            public void onPartialThinking(dev.langchain4j.model.chat.response.PartialThinking partialThinking) {
                if (partialThinking != null && partialThinking.text() != null) {
                    listener.onThinking(partialThinking.text());
                }
            }

            @Override
            public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                handleCompleteToolCall(completeToolCall, listener);
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                try {
                    AiMessage ai = response.aiMessage();
                    if (ai != null && ai.hasToolExecutionRequests()) {
                        toolCalls.addAll(ai.toolExecutionRequests());
                    }
                    processToolResults(toolCalls, context, listener);
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
                latch.countDown();
            }
        };
    }

    private void handlePartialResponse(String text, StringBuilder output, StringBuilder fullOutput, StreamListener listener) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (text.contains("<think>")) {
            int idx = text.indexOf("<think>");
            String before = text.substring(0, idx);
            if (!before.isBlank()) {
                output.append(before);
                fullOutput.append(before);
                listener.onToken(before);
            }
            thinkingBuffer.append(text.substring(idx + "<think>".length()));
        } else if (text.contains("</think>")) {
            int idx = text.indexOf("</think>");
            thinkingBuffer.append(text.substring(0, idx));
            String thinking = thinkingBuffer.toString();
            if (!thinking.isBlank()) {
                listener.onThinking(thinking);
            }
            thinkingBuffer.setLength(0);
            String after = text.substring(idx + "</think>".length());
            if (!after.isBlank()) {
                output.append(after);
                fullOutput.append(after);
                listener.onToken(after);
            }
        } else if (!thinkingBuffer.isEmpty()) {
            thinkingBuffer.append(text);
        } else {
            output.append(text);
            fullOutput.append(text);
            listener.onToken(text);
        }
    }

    private void handleCompleteToolCall(CompleteToolCall completeToolCall, StreamListener listener) {
        ToolExecutionRequest req = completeToolCall.toolExecutionRequest();
        String toolId = UUID.randomUUID().toString();
        toolIdToName.put(toolId, req.name());
        listener.onToolCallStart(toolId, req.name(), req.arguments());

        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() ->
                executeTool(req.name(), req.arguments())
        );
        pendingTools.put(toolId, future);
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

    private void processToolResults(List<ToolExecutionRequest> toolCalls, AgentContext context, StreamListener listener) {
        if (pendingTools.isEmpty()) {
            return;
        }

        CompletableFuture.allOf(pendingTools.values().toArray(new CompletableFuture[0])).join();

        List<ToolExecutionRequest> retryList = new ArrayList<>();

        Map<String, String> workingMap = new HashMap<>(toolIdToName);

        for (ToolExecutionRequest request : toolCalls) {
            String toolName = request.name();
            String args = request.arguments();

            String toolId = null;
            for (Map.Entry<String, String> entry : workingMap.entrySet()) {
                if (entry.getValue().equals(toolName)) {
                    toolId = entry.getKey();
                    workingMap.remove(toolId);
                    break;
                }
            }

            if (toolId == null) {
                log.warn("[Agent] toolId not found for tool={}", toolName);
                continue;
            }

            Object result = getToolResultById(toolId);
            listener.onToolCallEnd(toolId, toolName, result);

            if (isError(result)) {
                String errorMsg = result.toString();
                if (retryHandler.shouldRetry(toolName, args, errorMsg)) {
                    retryHandler.recordFailure(toolName, args);
                    int remaining = retryHandler.getRemainingRetries(toolName, args);
                    String reflection = retryHandler.buildReflectionMessage(toolName, errorMsg, remaining);
                    context.addAssistantMessage(reflection);
                    retryList.add(request);
                    log.info("[Agent] tool={} failed, will retry", toolName);
                } else {
                    context.addAssistantMessage(retryHandler.buildFinalErrorMessage(toolName, errorMsg));
                    log.warn("[Agent] tool={} failed after max retries, giving up", toolName);
                }
            } else {
                retryHandler.recordSuccess(toolName, args);
                context.addAssistantMessage("Tool: " + toolName + " Result: " + result);
                log.info("[Agent] tool={} result={}", toolName, result);
            }

            pendingTools.remove(toolId);
            toolIdToName.remove(toolId);
        }

        if (!retryList.isEmpty()) {
            log.info("[Agent] scheduling {} tool retries", retryList.size());
            retryList.forEach(req ->
                    context.addAssistantMessage("Retry: " + req.name() + " with arguments: " + req.arguments())
            );
        }
    }

    private Object getToolResultById(String toolId) {
        try {
            return pendingTools.get(toolId).get();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return "Error: " + cause.getMessage();
        }
    }

    private boolean isError(Object result) {
        return result.toString().startsWith("Error:");
    }

    private Object executeTool(String toolName, String argsJson) {
        try {
            String nodeId = toolRegistry.getNodeId(toolName);
            Map<String, Object> args = parseArgs(argsJson);
            return nodeExecutor.execute(nodeId, args);
        } catch (Exception e) {
            log.error("[Agent] tool={} execution error={}", toolName, e.getMessage());
            return "Error: " + e.getMessage();
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
