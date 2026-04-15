package io.autoflow.app.service;

import io.autoflow.app.model.AgentChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service interface for chat operations.
 */
public interface ChatService {

    String CHAT_FILE_EXTRACT_THREAD_POOL = "chat_file_extract_pool";

    /**
     * Process a chat request with SSE streaming.
     *
     * @param request the agent chat request
     * @return SSE emitter for streaming responses
     */
    SseEmitter chat(AgentChatRequest request);

    /**
     * Enriches the input text with content extracted from attached files.
     *
     * @param input the original input text
     * @param fileIds list of file IDs to extract text from
     * @return enriched input with file contents appended
     */
    String enrichInputWithFiles(String input, java.util.List<String> fileIds);
}
