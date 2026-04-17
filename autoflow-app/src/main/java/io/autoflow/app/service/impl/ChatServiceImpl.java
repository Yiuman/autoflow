package io.autoflow.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.model.chat.StreamingChatModel;
import io.autoflow.agent.ChatRequest;
import io.autoflow.agent.NodeExecutor;
import io.autoflow.agent.ReActAgent;
import io.autoflow.agent.ToolRegistry;
import io.autoflow.app.config.ModelRegistry;
import io.autoflow.app.listener.ChatStreamListener;
import io.autoflow.app.model.AgentChatRequest;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.model.FileResourceStream;
import io.autoflow.app.model.sse.AgentSSEEvent;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ChatService;
import io.autoflow.app.service.ChatSessionService;
import io.autoflow.app.service.FileResourceService;
import io.autoflow.plugin.textextractor.TextExtractParameter;
import io.autoflow.plugin.textextractor.TextExtractResult;
import io.autoflow.plugin.textextractor.TextExtractor;
import io.autoflow.spi.enums.MessageType;
import io.autoflow.spi.model.FileData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of ChatService that encapsulates chat business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String ERROR_TYPE = "error";

    private final ModelRegistry modelRegistry;
    private final ToolRegistry toolRegistry;
    private final NodeExecutor nodeExecutor;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final FileResourceService fileResourceService;

    @Override
    public SseEmitter chat(AgentChatRequest request) {
        log.info("Chat session started: sessionId={}", request.getSessionId());

        SseEmitter errorEmitter = validateRequest(request);
        if (errorEmitter != null) {
            return errorEmitter;
        }

        String sessionId = request.getSessionId();
        String conversationId = UUID.randomUUID().toString().replace("-", "");
        String input = request.getInput();
        List<String> fileIds = request.getFileIds();

        // Load session to get system prompt
        ChatSession session = chatSessionService.list(QueryWrapper.create()
                .eq(ChatSession::getId, sessionId)).stream().findFirst().orElse(null);
        String systemPrompt = session != null ? session.getSystemPrompt() : null;

        // Load history before saving current message to avoid including it
        List<io.autoflow.spi.model.ChatMessage> history = loadHistory(sessionId);
        String enrichedInput = enrichInputWithFiles(input, fileIds);

        ChatMessage userMessage = createUserMessage(sessionId, conversationId, input, fileIds);
        chatMessageService.save(userMessage);

        SseEmitter streamingEmitter = new SseEmitter(Long.MAX_VALUE);
        StreamingChatModel chatModel = resolveChatModel(request.getModelId());
        runAsyncChat(sessionId, conversationId, enrichedInput, systemPrompt, chatModel, streamingEmitter, history);

        return streamingEmitter;
    }

    @Override
    public String enrichInputWithFiles(String input, List<String> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return input;
        }

        DtpExecutor executor = DtpRegistry.getDtpExecutor(ChatService.CHAT_FILE_EXTRACT_THREAD_POOL);
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (String fileId : fileIds) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                FileResourceStream fileStream = fileResourceService.download(fileId);
                try {
                    FileData fileData = new FileData(fileStream.getFilename(), fileStream.getBytes());
                    TextExtractParameter param = new TextExtractParameter();
                    param.setFile(fileData);
                    TextExtractResult textResult = new TextExtractor().execute(param);
                    if (StrUtil.isNotBlank(textResult.getText())) {
                        log.info("Extracted text from file: fileId={}, filename={}", fileId, fileStream.getFilename());
                        return StrUtil.format("--- File: {} ---\n{}\n", fileStream.getFilename(), textResult.getText());
                    }
                    return "";
                } catch (Exception e) {
                    try {
                        return StrUtil.str(fileStream.getBytes(), StandardCharsets.UTF_8);
                    } catch (Throwable throwable) {
                        log.warn("Failed to extract text from file: fileId={}, error={}", fileId, throwable.getMessage());
                        return "";
                    }


                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        String extractedContent = futures.stream()
                .map(CompletableFuture::join)
                .filter(StrUtil::isNotBlank)
                .collect(java.util.stream.Collectors.joining());

        if (extractedContent.isEmpty()) {
            return input;
        }

        return input + "\n\n" + extractedContent;
    }

    private SseEmitter validateRequest(AgentChatRequest request) {
        String sessionId = request.getSessionId();

        if (sessionId == null || sessionId.isBlank()) {
            log.warn("Chat session rejected: sessionId is required");
            return sendErrorAndComplete("sessionId is required");
        }

        if (request.getInput() == null || request.getInput().isBlank()) {
            log.warn("Chat session rejected: sessionId={}, reason=blank input", sessionId);
            return sendErrorAndComplete("Input cannot be blank");
        }

        return null;
    }

    private ChatMessage createUserMessage(String sessionId, String conversationId, String content, List<String> fileIds) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setConversationId(conversationId);
        message.setRole("USER");
        message.setContent(content);
        if (CollUtil.isNotEmpty(fileIds)) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileIds", fileIds);
            Map<String, Map<String, Object>> filesInfo = new HashMap<>();
            for (String fileId : fileIds) {
                try {
                    io.autoflow.app.model.FileResource fileResource = fileResourceService.getInfo(fileId);
                    if (fileResource != null) {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", fileResource.getFilename());
                        fileInfo.put("size", fileResource.getSize());
                        fileInfo.put("mimeType", fileResource.getContentType());
                        filesInfo.put(fileId, fileInfo);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get file info: fileId={}", fileId, e);
                }
            }
            metadata.put("files", filesInfo);
            message.setMetadata(cn.hutool.json.JSONUtil.toJsonStr(metadata));
        }
        return message;
    }

    private StreamingChatModel resolveChatModel(String modelId) {
        StreamingChatModel chatModel = modelRegistry.getModel(modelId);
        log.info("Chat using model: modelId={}, actualModel={}", modelId, chatModel.getClass().getSimpleName());
        return chatModel;
    }

    private void runAsyncChat(String sessionId, String conversationId, String input, String systemPrompt,
                              StreamingChatModel chatModel, SseEmitter emitter,
                              List<io.autoflow.spi.model.ChatMessage> history) {
        ChatStreamListener listener = new ChatStreamListener(emitter, sessionId, conversationId, chatMessageService, chatSessionService);

        ReActAgent agent = ReActAgent.builder()
                .chatModel(chatModel)
                .systemPrompt(systemPrompt)
                .toolRegistry(toolRegistry)
                .nodeExecutor(nodeExecutor)
                .maxSteps(10)
                .maxToolRetries(3)
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                ChatRequest chatRequest = new ChatRequest(input, history);
                agent.chat(chatRequest, chatModel, listener);
            } finally {
                emitter.complete();
                log.info("Chat session ended: sessionId={}", sessionId);
            }
        });
    }

    private List<io.autoflow.spi.model.ChatMessage> loadHistory(String sessionId) {
        return chatMessageService.findBySessionId(sessionId).stream()
                .filter(msg -> !"ERROR".equals(msg.getRole()))
                .map(dbMsg -> {
                    io.autoflow.spi.model.ChatMessage spiMsg = new io.autoflow.spi.model.ChatMessage();
                    String content = dbMsg.getContent();
                    if ("USER".equals(dbMsg.getRole()) && StrUtil.isNotBlank(dbMsg.getMetadata())) {
                        try {
                            cn.hutool.json.JSONObject metaJson = cn.hutool.json.JSONUtil.parseObj(dbMsg.getMetadata());
                            List<String> metaFileIds = metaJson.getBeanList("fileIds", String.class);
                            if (CollUtil.isNotEmpty(metaFileIds)) {
                                content = enrichInputWithFiles(content, metaFileIds);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse metadata for message: {}", dbMsg.getId(), e);
                        }
                    }
                    spiMsg.setContent(content);
                    if ("USER".equals(dbMsg.getRole())) {
                        spiMsg.setType(MessageType.USER);
                    } else if ("ASSISTANT".equals(dbMsg.getRole())) {
                        spiMsg.setType(MessageType.ASSISTANT);
                    }
                    return spiMsg;
                })
                .toList();
    }

    private SseEmitter sendErrorAndComplete(String errorMessage) {
        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event()
                    .name(ERROR_TYPE)
                    .data(AgentSSEEvent.builder()
                            .type(ERROR_TYPE)
                            .content(errorMessage)
                            .build()));
        } catch (Exception e) {
            log.warn("Failed to send error event", e);
        }
        emitter.complete();
        return emitter;
    }
}
