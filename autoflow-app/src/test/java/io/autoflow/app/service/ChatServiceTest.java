package io.autoflow.app.service;

import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ChatService.
 *
 * @author autoflow
 * @date 2025/03/05
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ToolCallService toolCallService;

    @InjectMocks
    private ChatService chatService;

    private static final String SESSION_ID = "test-session-id";
    private static final String TYPE_MESSAGE = "message";
    private static final String TYPE_TOKEN = "token";
    private static final String TYPE_TOOL_CALL = "tool_call";

    private ChatMessage userMessage;
    private ChatMessage assistantMessage;
    private ChatMessage tokenMessage;
    private ChatMessage toolCallMessage;

    @BeforeEach
    void setUp() {
        userMessage = createChatMessage("1", SESSION_ID, "user", "Hello", TYPE_MESSAGE);
        assistantMessage = createChatMessage("2", SESSION_ID, "assistant", "Hi there!", TYPE_MESSAGE);
        tokenMessage = createChatMessage("3", SESSION_ID, "assistant", "token-content", TYPE_TOKEN);
        toolCallMessage = createChatMessage("4", SESSION_ID, "assistant", "tool-info", TYPE_TOOL_CALL);
    }

    @Test
    void testGetMessagesReturnsAllMessages() {
        List<ChatMessage> allMessages = Arrays.asList(userMessage, assistantMessage, tokenMessage, toolCallMessage);
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(allMessages);

        List<ChatMessage> result = chatService.getMessages(SESSION_ID);

        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(userMessage));
        assertTrue(result.contains(assistantMessage));
        assertTrue(result.contains(tokenMessage));
        assertTrue(result.contains(toolCallMessage));
    }

    @Test
    void testGetMessagesReturnsEmptyListWhenNoMessages() {
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        List<ChatMessage> result = chatService.getMessages(SESSION_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMessagesWithTypeFiltersByType() {
        List<ChatMessage> messageOnlyMessages = Arrays.asList(userMessage, assistantMessage);
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(messageOnlyMessages);

        List<ChatMessage> result = chatService.getMessages(SESSION_ID, TYPE_MESSAGE);

        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(msg -> assertEquals(TYPE_MESSAGE, msg.getType()));
    }

    @Test
    void testGetMessagesWithTypeReturnsEmptyWhenNoMatch() {
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        List<ChatMessage> result = chatService.getMessages(SESSION_ID, "nonexistent_type");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMessagesWithNullTypeReturnsAllMessages() {
        List<ChatMessage> allMessages = Arrays.asList(userMessage, assistantMessage, tokenMessage, toolCallMessage);
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(allMessages);

        List<ChatMessage> result = chatService.getMessages(SESSION_ID, null);

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    void testGetMessagesWithBlankTypeReturnsAllMessages() {
        List<ChatMessage> allMessages = Arrays.asList(userMessage, assistantMessage, tokenMessage, toolCallMessage);
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(allMessages);

        List<ChatMessage> result = chatService.getMessages(SESSION_ID, "");

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    void testGetMessagesWithToolCallType() {
        List<ChatMessage> toolCallMessages = List.of(toolCallMessage);
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(toolCallMessages);

        List<ChatMessage> result = chatService.getMessages(SESSION_ID, TYPE_TOOL_CALL);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TYPE_TOOL_CALL, result.get(0).getType());
    }

    @Test
    void testGetMessagesWithTokenType() {
        List<ChatMessage> tokenMessages = List.of(tokenMessage);
        when(chatMessageService.list(any(QueryWrapper.class))).thenReturn(tokenMessages);

        List<ChatMessage> result = chatService.getMessages(SESSION_ID, TYPE_TOKEN);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TYPE_TOKEN, result.get(0).getType());
    }

    private ChatMessage createChatMessage(final String id,
                                          final String sessionId,
                                          final String role,
                                          final String content,
                                          final String type) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setType(type);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
}
