package io.autoflow.app.rest;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import dev.langchain4j.model.chat.StreamingChatModel;
import io.autoflow.agent.ChatRequest;
import io.autoflow.agent.ReActAgent;
import io.autoflow.app.config.ModelRegistry;
import io.autoflow.app.listener.ChatStreamListener;
import io.autoflow.app.model.AgentChatRequest;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.FileResourceStream;
import io.autoflow.app.model.sse.AgentSSEEvent;
import io.autoflow.app.service.ChatMessageService;
import io.autoflow.app.service.ChatSessionService;
import io.autoflow.app.service.FileResourceService;
import io.autoflow.plugin.textextractor.TextExtractParameter;
import io.autoflow.plugin.textextractor.TextExtractResult;
import io.autoflow.plugin.textextractor.TextExtractor;
import io.autoflow.spi.enums.MessageType;
import io.autoflow.spi.model.FileData;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for agent chat with SSE streaming.
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ReActAgent reActAgent;
    private final ModelRegistry modelRegistry;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final FileResourceService fileResourceService;

    public ChatController(ReActAgent reActAgent, ModelRegistry modelRegistry,
                          ChatMessageService chatMessageService, ChatSessionService chatSessionService,
                          FileResourceService fileResourceService) {
        this.reActAgent = reActAgent;
        this.modelRegistry = modelRegistry;
        this.chatMessageService = chatMessageService;
        this.chatSessionService = chatSessionService;
        this.fileResourceService = fileResourceService;
    }

    private static final String ERROR_TYPE = "error";

    /**
     * Chat endpoint with SSE streaming.
     *
     * @param request the agent chat request
     * @return SSE emitter for streaming responses
     */
    @PostMapping
    public SseEmitter chat(@Valid @RequestBody AgentChatRequest request) {
        log.info("Chat session started: sessionId={}", request.getSessionId());

        SseEmitter emitter = validateAndCreateEmitter(request);
        if (emitter != null) {
            return emitter;
        }

        String sessionId = request.getSessionId();
        String conversationId = UUID.randomUUID().toString().replace("-", "");
        String input = request.getInput();
        List<String> fileIds = request.getFileIds();

        // Save user message with original input and fileIds in metadata (for display)
        ChatMessage userMessage = createUserMessage(sessionId, conversationId, input, fileIds);
        chatMessageService.save(userMessage);

        // Load history and enrich with file contents for agent context
        List<io.autoflow.spi.model.ChatMessage> history = loadHistory(sessionId);

        // Enrich current input with file contents for this turn
        String enrichedInput = enrichInputWithFiles(input, fileIds);

        SseEmitter streamingEmitter = new SseEmitter(Long.MAX_VALUE);
        StreamingChatModel chatModel = resolveChatModel(request.getModelId());
        runAsyncChat(sessionId, conversationId, enrichedInput, chatModel, streamingEmitter, history);

        return streamingEmitter;
    }

    private SseEmitter validateAndCreateEmitter(AgentChatRequest request) {
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

    /**
     * Enriches the input text with content extracted from attached files.
     *
     * @param input the original input text
     * @param fileIds list of file IDs to extract text from
     * @return enriched input with file contents appended
     */
    private String enrichInputWithFiles(String input, List<String> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return input;
        }

        List<String> fileContents = new ArrayList<>();
        TextExtractor textExtractor = new TextExtractor();

        for (String fileId : fileIds) {
            try {
                FileResourceStream fileStream = fileResourceService.download(fileId);
                FileData fileData = new FileData(fileStream.getFilename(), fileStream.getBytes());
                TextExtractParameter param = new TextExtractParameter();
                param.setFile(fileData);
                TextExtractResult result = textExtractor.execute(param);
                if (StrUtil.isNotBlank(result.getText())) {
                    fileContents.add(StrUtil.format("--- File: {} ---\n{}\n", fileStream.getFilename(), result.getText()));
                }
                log.info("Extracted text from file: fileId={}, filename={}", fileId, fileStream.getFilename());
            } catch (Exception e) {
                log.warn("Failed to extract text from file: fileId={}, error={}", fileId, e.getMessage());
            }
        }

        if (fileContents.isEmpty()) {
            return input;
        }

        return input + "\n\n" + String.join("\n", fileContents);
    }

    private ChatMessage createUserMessage(String sessionId, String conversationId, String content, List<String> fileIds) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setConversationId(conversationId);
        message.setRole("USER");
        message.setContent(content);
        if (CollUtil.isNotEmpty(fileIds)) {
            Map<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("fileIds", fileIds);
            // Enrich with file info for display
            Map<String, Map<String, Object>> filesInfo = new java.util.HashMap<>();
            for (String fileId : fileIds) {
                try {
                    io.autoflow.app.model.FileResource fileResource = fileResourceService.getInfo(fileId);
                    if (fileResource != null) {
                        Map<String, Object> fileInfo = new java.util.HashMap<>();
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

    private void runAsyncChat(String sessionId, String conversationId, String input, StreamingChatModel chatModel, SseEmitter emitter,
                              List<io.autoflow.spi.model.ChatMessage> history) {
        ChatStreamListener listener = new ChatStreamListener(emitter, sessionId, conversationId, chatMessageService, chatSessionService);

        CompletableFuture.runAsync(() -> {
            try {
                ChatRequest chatRequest = new ChatRequest(input, history);
                reActAgent.chat(chatRequest, chatModel, listener);
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
                    // Enrich USER messages that have fileIds in metadata
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
