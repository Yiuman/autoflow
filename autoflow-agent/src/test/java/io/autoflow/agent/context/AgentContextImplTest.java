package io.autoflow.agent.core.context;

import io.autoflow.spi.enums.MessageType;
import io.autoflow.spi.model.ChatMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentContextImplTest {

    @Nested
    class ConstructorTests {
        @Test
        void shouldCreateContextWithSessionId() {
            String sessionId = "test-session-123";
            AgentContextImpl context = new AgentContextImpl(sessionId);

            assertEquals(sessionId, context.getSessionId());
        }

        @Test
        void shouldInitializeWithZeroStepCount() {
            AgentContextImpl context = new AgentContextImpl("session-1");
            assertEquals(0, context.getStepCount());
        }
    }

    @Nested
    class MessageTests {
        @Test
        void shouldAddUserMessage() {
            AgentContextImpl context = new AgentContextImpl("session-1");
            context.addUserMessage("Hello");

            assertEquals(1, context.getMessages().size());
            ChatMessage message = context.getMessages().get(0);
            assertEquals(MessageType.USER, message.getType());
            assertEquals("Hello", message.getContent());
        }

        @Test
        void shouldAddAssistantMessage() {
            AgentContextImpl context = new AgentContextImpl("session-1");
            context.addAssistantMessage("Hi there");

            assertEquals(1, context.getMessages().size());
            ChatMessage message = context.getMessages().get(0);
            assertEquals(MessageType.ASSISTANT, message.getType());
            assertEquals("Hi there", message.getContent());
        }

        @Test
        void shouldPopulateMessagesListCorrectly() {
            AgentContextImpl context = new AgentContextImpl("session-1");
            context.addUserMessage("First message");
            context.addAssistantMessage("Second message");
            context.addUserMessage("Third message");

            assertEquals(3, context.getMessages().size());
            assertEquals(MessageType.USER, context.getMessages().get(0).getType());
            assertEquals(MessageType.ASSISTANT, context.getMessages().get(1).getType());
            assertEquals(MessageType.USER, context.getMessages().get(2).getType());
        }
    }

    @Nested
    class GetLastUserMessageTests {
        @Test
        void shouldReturnLastUserMessage() {
            AgentContextImpl context = new AgentContextImpl("session-1");
            context.addAssistantMessage("Ignore me");
            context.addUserMessage("User message 1");
            context.addAssistantMessage("Ignore me too");
            context.addUserMessage("User message 2");

            ChatMessage lastUserMsg = context.getLastUserMessage();
            assertNotNull(lastUserMsg);
            assertEquals(MessageType.USER, lastUserMsg.getType());
            assertEquals("User message 2", lastUserMsg.getContent());
        }

        @Test
        void shouldReturnNullWhenNoUserMessage() {
            AgentContextImpl context = new AgentContextImpl("session-1");
            context.addAssistantMessage("Only assistant message");

            assertNull(context.getLastUserMessage());
        }
    }

    @Nested
    class IncrementStepTests {
        @Test
        void shouldIncrementStepCount() {
            AgentContextImpl context = new AgentContextImpl("session-1");
            assertEquals(0, context.getStepCount());

            context.incrementStep();
            assertEquals(1, context.getStepCount());

            context.incrementStep();
            assertEquals(2, context.getStepCount());
        }
    }
}
