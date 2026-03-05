package io.autoflow.app.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.autoflow.app.chat.SimpleExecutionContext;
import io.autoflow.app.dto.event.ErrorEvent;
import io.autoflow.app.dto.event.MessageEvent;
import io.autoflow.app.dto.event.TokenEvent;
import io.autoflow.app.dto.event.ToolCallEvent;
import io.autoflow.app.llm.ChatModelFactory;
import io.autoflow.app.llm.ServiceToolFactory;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.model.ToolCall;
import io.autoflow.common.http.SSEContext;
import io.autoflow.spi.Services;
import io.autoflow.spi.context.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for managing chat sessions and messages with LLM integration.
 * Handles session creation, message sending, tool execution, and SSE streaming.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private static final String DEFAULT_PROVIDER = "openai";
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String ROLE_SYSTEM = "system";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_ERROR = "error";
    private static final String STATUS_SUCCESS = "success";
    private static final String SYSTEM_PROMPT = "You are a helpful AI assistant. "
            + "You can use available tools to help answer questions.";

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ToolCallService toolCallService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Creates a new chat session.
     *
     * @param title the title of the session (optional)
     * @return the created ChatSession
     */
    @Transactional
    public ChatSession createSession(final String title) {
        ChatSession session = new ChatSession();
        session.setTitle(StrUtil.isBlank(title) ? "New Chat" : title);
        session.setStatus(STATUS_ACTIVE);
        return chatSessionService.save(session);
    }

    /**
     * Sends a message in a chat session and gets the assistant's response.
     * This method saves the user message, calls the LLM, and saves the assistant response.
     *
     * @param sessionId   the session ID
     * @param userMessage the user's message content
     * @param provider    the LLM provider (optional, defaults to openai)
     * @return the assistant's response message
     */
    @Transactional
    public io.autoflow.app.model.ChatMessage sendMessage(final String sessionId,
                                                         final String userMessage,
                                                         final String provider) {
        // Get session by ID
        ChatSession session = chatSessionService.get(sessionId);
        if (Objects.isNull(session)) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        // Save user message
        io.autoflow.app.model.ChatMessage userMsg =
                createAndSaveMessage(sessionId, ROLE_USER, userMessage);

        // Get message history
        List<io.autoflow.app.model.ChatMessage> history = getMessages(sessionId);
        List<dev.langchain4j.data.message.ChatMessage> langChainMessages = buildMessageHistory(history);

        // Get chat model and tools
        String providerName = StrUtil.isBlank(provider) ? DEFAULT_PROVIDER : provider;
        ChatModel chatModel = ChatModelFactory.getModel(providerName, new HashMap<>());
        List<ToolSpecification> tools = ServiceToolFactory.getAvailableTools();

        // Call LLM with tools
        ChatResponse response = callLLMWithTools(chatModel, langChainMessages, tools, sessionId);

        // Save assistant response
        String assistantContent = response.aiMessage().text();
        io.autoflow.app.model.ChatMessage assistantMsg =
                createAndSaveMessage(sessionId, ROLE_ASSISTANT, assistantContent);
        chatSessionService.save(session);

        return assistantMsg;
    }

    /**
     * Gets all messages for a session.
     *
     * @param sessionId the session ID
     * @return list of messages in chronological order
     */
    public List<io.autoflow.app.model.ChatMessage> getMessages(final String sessionId) {
        return chatMessageService.list(QueryWrapper.create().eq("session_id", sessionId));
    }

    /**
     * Gets messages for a session filtered by type.
     *
     * @param sessionId the session ID
     * @param type      the message type to filter by (optional)
     * @return list of messages filtered by type in chronological order
     */
    public List<io.autoflow.app.model.ChatMessage> getMessages(final String sessionId, final String type) {
        QueryWrapper wrapper = QueryWrapper.create().eq("session_id", sessionId);
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        return chatMessageService.list(wrapper);
    }

    /**
     * Gets a specific session by ID.
     *
     * @param sessionId the session ID
     * @return the session
     */
    public ChatSession getSession(final String sessionId) {
        return chatSessionService.get(sessionId);
    }

    /**
     * Deletes a chat session.
     *
     * @param sessionId the session ID
     */
    public void deleteSession(final String sessionId) {
        chatSessionService.delete(sessionId);
    }

    /**
     * Gets all chat sessions.
     *
     * @return list of all sessions
     */
    public List<ChatSession> getSessions() {
        return chatSessionService.list();
    }

    /**
     * Executes a tool by name with the given parameters.
     *
     * @param toolName the name of the tool to execute
     * @param params   the parameters for the tool
     * @return the ToolCall record with execution result
     */
    public ToolCall executeTool(final String toolName, final Map<String, Object> params) {
        io.autoflow.spi.Service<?> service = Services.getService(toolName);
        if (Objects.isNull(service)) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }

        ToolCall toolCall = new ToolCall();
        toolCall.setToolName(toolName);
        toolCall.setParameters(JSONUtil.toJsonStr(params));
        toolCall.setStatus("running");
        toolCall.setCreatedAt(LocalDateTime.now());

        try {
            ExecutionContext ctx = new SimpleExecutionContext(params);
            Object result = service.execute(ctx);
            toolCall.setResult(JSONUtil.toJsonStr(result));
            toolCall.setStatus(STATUS_SUCCESS);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            toolCall.setResult(JSONUtil.toJsonStr(Map.of("error", e.getMessage())));
            toolCall.setStatus(STATUS_ERROR);
        }

        return toolCallService.save(toolCall);
    }

    /**
     * Creates an SSE emitter for real-time chat events.
     *
     * @param sessionId the session ID
     * @return the SseEmitter for streaming events
     */
    public SseEmitter createSseEmitter(final String sessionId) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        SSEContext.add(sessionId, sseEmitter);

        sseEmitter.onCompletion(() -> SSEContext.close(sessionId));
        sseEmitter.onTimeout(() -> SSEContext.close(sessionId));
        sseEmitter.onError(e -> SSEContext.close(sessionId));

        return sseEmitter;
    }

    /**
     * Sends a message asynchronously with SSE streaming.
     *
     * @param sessionId   the session ID
     * @param userMessage the user's message
     * @param provider    the LLM provider
     * @return the SseEmitter for streaming events
     */
    public SseEmitter sendMessageAsync(final String sessionId,
                                       final String userMessage,
                                       final String provider) {
        SseEmitter sseEmitter = createSseEmitter(sessionId);

        executorService.submit(() -> {
            try {
                // Send user message event
                sendSseEvent(sessionId, new MessageEvent(sessionId, ROLE_USER, userMessage));

                io.autoflow.app.model.ChatMessage response = sendMessage(sessionId, userMessage, provider);

                // Send assistant message event
                sendSseEvent(sessionId, new MessageEvent(
                        sessionId, ROLE_ASSISTANT, response.getContent(), response.getId()));

                // Send completion event
                sseEmitter.complete();
            } catch (Exception e) {
                log.error("Error sending message", e);
                sendSseEvent(sessionId, new ErrorEvent(sessionId, "SEND_ERROR", e.getMessage()));
                sseEmitter.completeWithError(e);
            }
        });

        return sseEmitter;
    }

    /**
     * Sends a message with TRUE streaming (token-by-token) using SSE.
     * This uses the StreamingChatModel API to get real-time token streaming.
     *
     * @param sessionId   the session ID
     * @param userMessage the user's message
     * @param provider    the LLM provider
     * @return the SseEmitter for streaming events
     */
    @SuppressWarnings("checkstyle:MethodLength")
    public SseEmitter sendMessageStreaming(final String sessionId,
                                           final String userMessage,
                                           final String provider) {
        SseEmitter sseEmitter = createSseEmitter(sessionId);

        executorService.submit(() -> {
            try {
                // Get session by ID
                ChatSession session = chatSessionService.get(sessionId);
                if (Objects.isNull(session)) {
                    throw new IllegalArgumentException("Session not found: " + sessionId);
                }

                // Save user message
                createAndSaveMessage(sessionId, ROLE_USER, userMessage);

                // Send user message event
                sendSseEvent(sessionId, new MessageEvent(sessionId, ROLE_USER, userMessage));

                // Get message history
                List<io.autoflow.app.model.ChatMessage> history = getMessages(sessionId);
                List<dev.langchain4j.data.message.ChatMessage> langChainMessages = buildMessageHistory(history);

                // Get streaming chat model and tools
                String providerName = StrUtil.isBlank(provider) ? DEFAULT_PROVIDER : provider;
                StreamingChatModel streamingModel = ChatModelFactory.getStreamingModel(providerName, new HashMap<>());
                List<ToolSpecification> tools = ServiceToolFactory.getAvailableTools();

                // StringBuilder to accumulate the full response
                StringBuilder fullResponse = new StringBuilder();

                // Create streaming response handler
                StreamingChatResponseHandler handler = new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(final String partialResponse) {
                        // Stream each token via SSE
                        fullResponse.append(partialResponse);
                        sendSseEvent(sessionId, new TokenEvent(sessionId, partialResponse));
                    }

                    @Override
                    public void onCompleteResponse(final ChatResponse completeResponse) {
                        try {
                            AiMessage aiMessage = completeResponse.aiMessage();

                            // Handle tool calls if present
                            if (aiMessage.hasToolExecutionRequests()) {
                                handleToolCallsStreaming(
                                        streamingModel, langChainMessages, tools, sessionId, aiMessage, fullResponse);
                            } else {
                                // Save assistant response
                                String assistantContent = fullResponse.toString();
                                io.autoflow.app.model.ChatMessage assistantMsg =
                                        createAndSaveMessage(sessionId, ROLE_ASSISTANT, assistantContent);

                                // Send completion message event
                                sendSseEvent(sessionId, new MessageEvent(
                                        sessionId, ROLE_ASSISTANT, assistantContent, assistantMsg.getId()));
                            }

                            // Update session
                            chatSessionService.save(session);

                            sseEmitter.complete();
                        } catch (Exception e) {
                            log.error("Error completing streaming response", e);
                            sendSseEvent(sessionId, new ErrorEvent(sessionId, "STREAM_ERROR", e.getMessage()));
                            sseEmitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(final Throwable error) {
                        log.error("Streaming error", error);
                        sendSseEvent(sessionId, new ErrorEvent(sessionId, "STREAM_ERROR", error.getMessage()));
                        sseEmitter.completeWithError(error);
                    }
                };

                // Build request and start streaming
                ChatRequest request = ChatRequest.builder()
                        .messages(langChainMessages)
                        .toolSpecifications(tools)
                        .build();

                streamingModel.chat(request, handler);

            } catch (Exception e) {
                log.error("Error sending streaming message", e);
                sendSseEvent(sessionId, new ErrorEvent(sessionId, "SEND_ERROR", e.getMessage()));
                sseEmitter.completeWithError(e);
            }
        });

        return sseEmitter;
    }

    /**
     * Handles tool calls during streaming.
     */
    @SuppressWarnings("unchecked")
    private void handleToolCallsStreaming(final StreamingChatModel streamingModel,
                                         final List<dev.langchain4j.data.message.ChatMessage> messages,
                                         final List<ToolSpecification> tools,
                                         final String sessionId,
                                         final AiMessage aiMessage,
                                         final StringBuilder fullResponse) {
        List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();
        List<ToolExecutionResultMessage> toolResults = new ArrayList<>();

        for (ToolExecutionRequest request : toolRequests) {
            String toolName = request.name();
            String arguments = request.arguments();

            // Send tool call event
            sendSseEvent(sessionId, new ToolCallEvent(sessionId, toolName, arguments, null, "executing"));

            // Execute the tool
            Map<String, Object> params = JSONUtil.toBean(arguments, Map.class);
            ToolCall toolCall = executeTool(toolName, params);

            // Send tool result event
            sendSseEvent(sessionId, new ToolCallEvent(
                    sessionId, toolName, arguments, toolCall.getResult(), toolCall.getStatus()));

            toolResults.add(new ToolExecutionResultMessage(toolName, toolName + "_call", toolCall.getResult()));
        }

        // Continue conversation with tool results
        List<dev.langchain4j.data.message.ChatMessage> newMessages = new ArrayList<>(messages);
        newMessages.add(aiMessage);
        newMessages.addAll(toolResults);

        ChatRequest request = ChatRequest.builder()
                .messages(newMessages)
                .toolSpecifications(tools)
                .build();

        streamingModel.chat(request, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(final String partialResponse) {
                fullResponse.append(partialResponse);
                sendSseEvent(sessionId, new TokenEvent(sessionId, partialResponse));
            }

            @Override
            public void onCompleteResponse(final ChatResponse completeResponse) {
                // Save assistant response
                String assistantContent = fullResponse.toString();
                io.autoflow.app.model.ChatMessage assistantMsg =
                        createAndSaveMessage(sessionId, ROLE_ASSISTANT, assistantContent);

                // Send completion message event
                sendSseEvent(sessionId, new MessageEvent(
                        sessionId, ROLE_ASSISTANT, assistantContent, assistantMsg.getId()));

                // Update session
                ChatSession session = chatSessionService.get(sessionId);
                if (Objects.nonNull(session)) {
                    chatSessionService.save(session);
                }
            }

            @Override
            public void onError(final Throwable error) {
                log.error("Error in tool call streaming", error);
                sendSseEvent(sessionId, new ErrorEvent(sessionId, "TOOL_ERROR", error.getMessage()));
            }
        });
    }

    private io.autoflow.app.model.ChatMessage createAndSaveMessage(final String sessionId,
                                                                   final String role,
                                                                   final String content) {
        io.autoflow.app.model.ChatMessage message = new io.autoflow.app.model.ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        return chatMessageService.save(message);
    }

    private List<dev.langchain4j.data.message.ChatMessage> buildMessageHistory(
            final List<io.autoflow.app.model.ChatMessage> history) {
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));

        for (io.autoflow.app.model.ChatMessage msg : history) {
            switch (msg.getRole()) {
                case ROLE_USER -> messages.add(new UserMessage(msg.getContent()));
                case ROLE_ASSISTANT -> messages.add(new AiMessage(msg.getContent()));
                case ROLE_SYSTEM -> messages.add(new SystemMessage(msg.getContent()));
                default -> log.warn("Unknown message role: {}", msg.getRole());
            }
        }

        return messages;
    }

    @SuppressWarnings("unchecked")
    private ChatResponse callLLMWithTools(final ChatModel chatModel,
                                         final List<dev.langchain4j.data.message.ChatMessage> messages,
                                         final List<ToolSpecification> tools,
                                         final String sessionId) {
        ChatRequest.Builder requestBuilder = ChatRequest.builder()
                .messages(messages)
                .toolSpecifications(tools);

        ChatResponse response = chatModel.chat(requestBuilder.build());
        AiMessage aiMessage = response.aiMessage();

        // Handle tool calls if present
        while (aiMessage.hasToolExecutionRequests()) {
            List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();
            List<ToolExecutionResultMessage> toolResults = new ArrayList<>();

            for (ToolExecutionRequest request : toolRequests) {
                String toolName = request.name();
                String arguments = request.arguments();

                // Send tool call event
                sendSseEvent(sessionId, new ToolCallEvent(sessionId, toolName, arguments, null, "executing"));

                // Execute the tool
                Map<String, Object> params = JSONUtil.toBean(arguments, Map.class);
                ToolCall toolCall = executeTool(toolName, params);

                // Send tool result event
                sendSseEvent(sessionId, new ToolCallEvent(
                        sessionId, toolName, arguments, toolCall.getResult(), toolCall.getStatus()));

                toolResults.add(new ToolExecutionResultMessage(toolName, toolName + "_call", toolCall.getResult()));
            }

            // Continue conversation with tool results
            List<dev.langchain4j.data.message.ChatMessage> newMessages = new ArrayList<>(messages);
            newMessages.add(aiMessage);
            newMessages.addAll(toolResults);

            requestBuilder = ChatRequest.builder()
                    .messages(newMessages)
                    .toolSpecifications(tools);

            response = chatModel.chat(requestBuilder.build());
            aiMessage = response.aiMessage();
        }

        return response;
    }

    private void sendSseEvent(final String sessionId, final Object event) {
        SseEmitter sseEmitter = SSEContext.get(sessionId);
        if (Objects.nonNull(sseEmitter)) {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name(event.getClass().getSimpleName())
                        .data(JSONUtil.toJsonStr(event)));
            } catch (Exception e) {
                log.warn("Failed to send SSE event for session: {}", sessionId, e);
            }
        }
    }
}
