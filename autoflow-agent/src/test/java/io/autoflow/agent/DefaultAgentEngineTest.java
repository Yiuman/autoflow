package io.autoflow.agent.engine;

import io.autoflow.agent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultAgentEngine with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class DefaultAgentEngineTest {

    @Mock
    private Reasoner reasoner;

    @Mock
    private ActionParser actionParser;

    @Mock
    private NodeExecutor nodeExecutor;

    @Mock
    private MemoryStore memoryStore;

    @Mock
    private ToolRegistry toolRegistry;

    @Mock
    private StreamListener listener;

    private DefaultAgentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultAgentEngine(memoryStore, reasoner, actionParser, nodeExecutor, toolRegistry);
    }

    @Test
    void chat_singleToolCallFlow_tokensAndToolCallbacksVerified() {
        // Arrange
        String sessionId = "test-session";
        String userInput = "Hello";
        String toolName = "my_tool";
        String nodeId = "node-123";
        Object toolResult = "result_value";
        String toolCallOutput = "{\"action\":\"call_tool\",\"tool\":\"my_tool\",\"args\":{}}";
        String finishOutput = "{\"action\":\"finish\"}";

        AgentAction toolAction = new AgentAction();
        toolAction.setAction("call_tool");
        toolAction.setTool(toolName);
        toolAction.setArgs(Map.of());

        AgentAction finishAction = new AgentAction();
        finishAction.setAction("finish");

        when(memoryStore.load(sessionId)).thenReturn(null);
        doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(toolCallOutput);
            return null;
        }).doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(finishOutput);
            return null;
        }).when(reasoner).think(any(AgentContext.class), any(StreamListener.class));
        when(actionParser.parse(toolCallOutput)).thenReturn(toolAction);
        when(actionParser.parse(finishOutput)).thenReturn(finishAction);
        when(toolRegistry.getNodeId(toolName)).thenReturn(nodeId);
        when(nodeExecutor.execute(eq(nodeId), any())).thenReturn(toolResult);

        // Act
        engine.chat(sessionId, userInput, listener);

        // Assert
        verify(memoryStore).load(sessionId);
        verify(memoryStore).save(any(AgentContext.class));

        // Verify onToken was called (accumulated output) - at least once since tokens accumulate
        verify(listener, atLeastOnce()).onToken(anyString());

        // Verify onToolStart was called for the tool
        verify(listener).onToolStart(toolName);

        // Verify onToolEnd was called with correct tool name and result
        ArgumentCaptor<Object> resultCaptor = ArgumentCaptor.forClass(Object.class);
        verify(listener).onToolEnd(eq(toolName), resultCaptor.capture());
        assertEquals(toolResult, resultCaptor.getValue());

        // Verify onComplete is called at the end
        verify(listener).onComplete();

        // Verify no errors
        verify(listener, never()).onError(any(Throwable.class));
    }

    @Test
    void chat_maxStepsLimitEnforced_onlyTwoStepsExecuted() {
        // Arrange
        String sessionId = "test-session";
        int maxSteps = 2;
        engine = new DefaultAgentEngine(memoryStore, reasoner, actionParser, nodeExecutor, toolRegistry, maxSteps);

        String toolName = "my_tool";
        String nodeId = "node-123";
        String llmOutput = "{\"action\":\"call_tool\",\"tool\":\"my_tool\",\"args\":{}}";

        AgentAction action = new AgentAction();
        action.setAction("call_tool");
        action.setTool(toolName);
        action.setArgs(Map.of());

        when(memoryStore.load(sessionId)).thenReturn(null);
        doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(llmOutput);
            return null;
        }).when(reasoner).think(any(AgentContext.class), any(StreamListener.class));
        when(actionParser.parse(llmOutput)).thenReturn(action);
        when(toolRegistry.getNodeId(toolName)).thenReturn(nodeId);
        when(nodeExecutor.execute(eq(nodeId), any())).thenReturn("result");

        // Act
        engine.chat(sessionId, "Hello", listener);

        // Assert - reasoner.think should be called exactly maxSteps times
        verify(reasoner, times(maxSteps)).think(any(AgentContext.class), any(StreamListener.class));
        verify(nodeExecutor, times(maxSteps)).execute(eq(nodeId), any());
        verify(listener).onComplete();
    }

    @Test
    void chat_finishAction_stopsImmediately() {
        // Arrange
        String sessionId = "test-session";
        String llmOutput = "{\"action\":\"finish\",\"tool\":null,\"args\":{}}";

        AgentAction finishAction = new AgentAction();
        finishAction.setAction("finish");

        when(memoryStore.load(sessionId)).thenReturn(null);
        doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(llmOutput);
            return null;
        }).when(reasoner).think(any(AgentContext.class), any(StreamListener.class));
        when(actionParser.parse(llmOutput)).thenReturn(finishAction);

        // Act
        engine.chat(sessionId, "Hello", listener);

        // Assert - should stop after one step without calling any tool
        verify(reasoner, times(1)).think(any(AgentContext.class), any(StreamListener.class));
        verify(nodeExecutor, never()).execute(any(), any());
        verify(listener).onComplete();
    }

    @Test
    void chat_toolNotFound_nodeExecutorThrowsException_errorCallbackInvoked() {
        // Arrange
        String sessionId = "test-session";
        String toolName = "unknown_tool";
        String llmOutput = "{\"action\":\"call_tool\",\"tool\":\"unknown_tool\",\"args\":{}}";

        AgentAction action = new AgentAction();
        action.setAction("call_tool");
        action.setTool(toolName);
        action.setArgs(Map.of());

        when(memoryStore.load(sessionId)).thenReturn(null);
        doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(llmOutput);
            return null;
        }).when(reasoner).think(any(AgentContext.class), any(StreamListener.class));
        when(actionParser.parse(llmOutput)).thenReturn(action);
        when(toolRegistry.getNodeId(toolName)).thenReturn(null); // tool not found
        when(nodeExecutor.execute(isNull(), any())).thenThrow(new NullPointerException("nodeId is null"));

        // Act
        engine.chat(sessionId, "Hello", listener);

        // Assert - error callback should be invoked
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(listener).onError(errorCaptor.capture());
        assertTrue(errorCaptor.getValue() instanceof NullPointerException);

        // onComplete should NOT be called when error occurs
        verify(listener, never()).onComplete();
    }

    @Test
    void chat_nullActionFromParser_stopsWithoutToolExecution() {
        // Arrange
        String sessionId = "test-session";
        String llmOutput = "some unparseable output";

        when(memoryStore.load(sessionId)).thenReturn(null);
        doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(llmOutput);
            return null;
        }).when(reasoner).think(any(AgentContext.class), any(StreamListener.class));
        when(actionParser.parse(llmOutput)).thenReturn(null);

        // Act
        engine.chat(sessionId, "Hello", listener);

        // Assert - should stop without tool execution
        verify(reasoner, times(1)).think(any(AgentContext.class), any(StreamListener.class));
        verify(nodeExecutor, never()).execute(any(), any());
        verify(listener).onComplete();
    }

    @Test
    void chat_existingSession_loadsExistingContext() {
        // Arrange
        String sessionId = "test-session";
        AgentContext existingContext = new AgentContext(sessionId);
        existingContext.addUserMessage("previous message");

        when(memoryStore.load(sessionId)).thenReturn(existingContext);
        doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken("{\"action\":\"finish\"}");
            return null;
        }).when(reasoner).think(any(AgentContext.class), any(StreamListener.class));
        when(actionParser.parse(any())).thenReturn(new AgentAction() {{ setAction("finish"); }});

        // Act
        engine.chat(sessionId, "New message", listener);

        // Assert
        verify(memoryStore).load(sessionId);
        verify(memoryStore).save(any(AgentContext.class)); // context should be saved
        verify(listener).onComplete();
    }

    @Test
    void chat_reactWithSystemPrompt_multiStepToolCalls() {
        // Arrange
        String sessionId = "test-session";
        String systemPrompt = "You are a helpful assistant. When you need to perform actions, " +
                "respond with JSON in this format: {\"action\":\"call_tool\",\"tool\":\"tool_name\",\"args\":{}}. " +
                "When finished, respond with: {\"action\":\"finish\"}.";

        engine = new DefaultAgentEngine(memoryStore, reasoner, actionParser, nodeExecutor, toolRegistry, 5);

        AgentContext ctx = new AgentContext(sessionId);
        ctx.setSystemPrompt(systemPrompt);
        ctx.addUserMessage("What's the weather?");

        // Simulate: LLM calls weather tool -> returns result -> LLM finishes
        String weatherCall = "{\"action\":\"call_tool\",\"tool\":\"get_weather\",\"args\":{\"city\":\"Beijing\"}}";
        String finishCall = "{\"action\":\"finish\"}";

        AgentAction weatherAction = new AgentAction();
        weatherAction.setAction("call_tool");
        weatherAction.setTool("get_weather");
        weatherAction.setArgs(Map.of("city", "Beijing"));

        AgentAction finishAction = new AgentAction();
        finishAction.setAction("finish");

        when(memoryStore.load(sessionId)).thenReturn(ctx);
        // First step: weather tool, second step: finish
        doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(weatherCall);
            return null;
        }).doAnswer(invocation -> {
            StreamListener sl = invocation.getArgument(1);
            sl.onToken(finishCall);
            return null;
        }).when(reasoner).think(any(AgentContext.class), any(StreamListener.class));

        when(actionParser.parse(weatherCall)).thenReturn(weatherAction);
        when(actionParser.parse(finishCall)).thenReturn(finishAction);
        when(toolRegistry.getNodeId("get_weather")).thenReturn("weather-node");
        when(nodeExecutor.execute(eq("weather-node"), any())).thenReturn("Sunny, 25C");

        // Act
        engine.chat(sessionId, "What's the weather?", listener);

        // Assert
        verify(memoryStore).load(sessionId);
        verify(memoryStore).save(any(AgentContext.class));
        verify(listener).onToolStart("get_weather");
        verify(listener).onToolEnd(eq("get_weather"), eq("Sunny, 25C"));
        verify(listener).onComplete();
        verify(listener, never()).onError(any(Throwable.class));
    }
}
