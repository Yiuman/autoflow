package io.autoflow.app.rest;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.dto.ChatRequest;
import io.autoflow.app.dto.SendMessageRequest;
import io.autoflow.app.model.ChatMessage;
import io.autoflow.app.model.ChatSession;
import io.autoflow.app.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ChatController SSE endpoints.
 *
 * @author autoflow
 * @date 2025/03/05
 */
class ChatControllerTest {

    private transient MockMvc mockMvc;
    private transient ChatService chatService;

    private transient ChatSession testSession;
    private transient ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        ChatController chatController = new ChatController(chatService);
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();

        testSession = new ChatSession();
        testSession.setId("test-session-id");
        testSession.setTitle("Test Chat");
        testSession.setStatus("active");
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setUpdatedAt(LocalDateTime.now());

        testMessage = new ChatMessage();
        testMessage.setId("test-message-id");
        testMessage.setSessionId("test-session-id");
        testMessage.setRole("user");
        testMessage.setContent("Hello, world!");
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateSession() throws Exception {
        when(chatService.createSession("Test Chat")).thenReturn(testSession);

        Map<String, String> request = new HashMap<>();
        request.put("title", "Test Chat");

        mockMvc.perform(post("/api/chat/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateSessionWithoutTitle() throws Exception {
        when(chatService.createSession(null)).thenReturn(testSession);

        mockMvc.perform(post("/api/chat/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetMessages() throws Exception {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(testMessage);

        when(chatService.getMessages("test-session-id")).thenReturn(messages);

        mockMvc.perform(get("/api/chat/sessions/test-session-id/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetMessagesEmptySession() throws Exception {
        when(chatService.getMessages("empty-session-id")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/chat/sessions/empty-session-id/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testSendMessageReturnsSseEmitter() throws Exception {
        SseEmitter mockEmitter = new SseEmitter();
        when(chatService.sendMessageStreaming("test-session-id", "Test message", null)).thenReturn(mockEmitter);

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("Test message");
        request.setProvider(null);

        mockMvc.perform(post("/api/chat/sessions/test-session-id/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSessions() throws Exception {
        List<ChatSession> sessions = new ArrayList<>();
        sessions.add(testSession);

        when(chatService.getSessions()).thenReturn(sessions);

        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testSendMessageSync() throws Exception {
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setId("assistant-message-id");
        assistantMessage.setSessionId("test-session-id");
        assistantMessage.setRole("assistant");
        assistantMessage.setContent("Hello! How can I help you?");
        assistantMessage.setCreatedAt(LocalDateTime.now());

        when(chatService.sendMessage("test-session-id", "Hello", null)).thenReturn(assistantMessage);

        ChatRequest request = new ChatRequest();
        request.setMessage("Hello");
        request.setProvider(null);

        mockMvc.perform(post("/api/chat/sessions/test-session-id/messages/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testExecuteTool() throws Exception {
        io.autoflow.app.model.ToolCall toolCall = new io.autoflow.app.model.ToolCall();
        toolCall.setId("tool-call-id");
        toolCall.setToolName("test-tool");
        toolCall.setParameters("{\"param1\": \"value1\"}");
        toolCall.setResult("{\"result\": \"success\"}");
        toolCall.setStatus("success");
        toolCall.setCreatedAt(LocalDateTime.now());

        Map<String, Object> params = new HashMap<>();
        params.put("param1", "value1");
        
        when(chatService.executeTool("test-tool", params)).thenReturn(toolCall);

        ChatController.ToolExecutionRequest request = new ChatController.ToolExecutionRequest();
        request.setToolName("test-tool");
        request.setParameters(params);

        mockMvc.perform(post("/api/chat/tools/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
