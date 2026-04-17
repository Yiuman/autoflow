package io.autoflow.agent;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.provider.ChatModelProviders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ReAct flow with real LLM.
 * Uses MiniMax API for actual streaming responses.
 */
class ReActIntegrationTest {

    private ReActAgent engine;

    @BeforeEach
    void setUp() {
        ToolRegistry toolRegistry = createToolRegistry();
        StreamingChatModel streamingChatModel = createStreamingChatModel();

        engine = ReActAgent.builder()
                .chatModel(streamingChatModel)
                .nodeExecutor(new TestNodeExecutor())
                .toolRegistry(toolRegistry)
                .maxSteps(10)
                .build();
    }

    private static final String DEFAULT_PROMPT = """
        You are a helpful AI assistant with access to tools.

        ## Guidelines
        1. Think step-by-step before taking action - use Thought to reason through the problem
        2. Use tools only when necessary - if you know the answer, respond directly
        3. When a tool fails, acknowledge the error, reflect on what went wrong, and try alternative approaches
        4. Be concise but thorough in your reasoning

        ## Response Format
        When using tools, follow this format:

        Question: {user_question}
        Thought: [Describe your reasoning - what you know, what you need to find out, and your plan]
        Action: [Tool name from available tools, only if needed]
        Action Input: [Arguments in JSON format]
        Observation: [Result will appear here after tool execution]
        ... (Thought/Action/Observation can repeat as needed)

        Thought: Based on my reasoning and observations, I now have the answer.
        Final Answer: [Your concise response to the user]

        ## Self-Correction
        If a tool fails or returns an unexpected result:

        1. **Reflect**: Analyze what went wrong:
           - Was the tool called with wrong arguments?
           - Is there a different tool that could achieve the same goal?
           - Is the task even possible with available tools?

        2. **Plan Fix**: Determine an alternative approach

        3. **Retry**: Call a different tool or same tool with corrected arguments

        Example of self-correction after tool failure:
        ```
        Thought: The calculator returned "Error: division by zero".\
        Reflection: I tried to divide by zero. I need to check if the divisor is valid before dividing.
        Action: evaluate
        Action Input: {"expression": "if(b != 0, a / b, 'undefined')", "a": 10, "b": 0}
        ...
        ```

        ## Important
        - When you have completed the task, provide your Final Answer
        - Maximum 3 reflection attempts per failed tool call
        """;

    private ChatRequest createRequest(String input) {
        return new ChatRequest(input, new ArrayList<>(), DEFAULT_PROMPT);
    }

    private ToolRegistry createToolRegistry() {
        return new ToolRegistry() {
            @Override
            public List<ToolSpecification> getToolSpecifications() {
                return List.of(
                        ToolSpecification.builder()
                                .name("calculate")
                                .description("Perform basic arithmetic calculations. Input: two integers a and b, and operation (add/subtract/multiply/divide)")
                                .parameters(JsonObjectSchema.builder()
                                        .addIntegerProperty("a")
                                        .addIntegerProperty("b")
                                        .addStringProperty("operation")
                                        .build())
                                .build(),
                        ToolSpecification.builder()
                                .name("get_weather")
                                .description("Get current weather for a city. Input: city name")
                                .parameters(JsonObjectSchema.builder()
                                        .addStringProperty("city")
                                        .build())
                                .build(),
                        ToolSpecification.builder()
                                .name("get_current_time")
                                .description("Get current time. Input: timezone (e.g., Asia/Shanghai)")
                                .parameters(JsonObjectSchema.builder()
                                        .addStringProperty("timezone")
                                        .build())
                                .build(),
                        ToolSpecification.builder()
                                .name("search")
                                .description("Search the web for information. Input: query string")
                                .parameters(JsonObjectSchema.builder()
                                        .addStringProperty("query")
                                        .build())
                                .build(),
                        ToolSpecification.builder()
                                .name("translate")
                                .description("Translate text from one language to another. Input: text, source language, target language")
                                .parameters(JsonObjectSchema.builder()
                                        .addStringProperty("text")
                                        .addStringProperty("from")
                                        .addStringProperty("to")
                                        .build())
                                .build()
                );
            }

            @Override
            public String getNodeId(String toolName) {
                return toolName;
            }
        };
    }

    private StreamingChatModel createStreamingChatModel() {
        String baseUrl = "https://api.minimaxi.com/v1";
        String apiKey = "sk-cp-ZykFe8GXljYHCaU4rweMsxudV_0b7z-2kRvSlseG4Mflyp5oCfpv_cedXqRQtPnAZykcJXqCgMZx-dMaxPX0QrDbRVp_hMhLzT2yJkZqITfaw2gWvL_Biww";
        String modelName = "MiniMax-M2.7";

        Map<String, Object> parameter = new HashMap<>();
        parameter.put("baseUrl", baseUrl);
        parameter.put("apiKey", apiKey);
        parameter.put("modelName", modelName);

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setModelName(modelName);
        modelConfig.setImplClass("io.autoflow.plugin.llm.provider.openai.OpenAiChatModelProvider");

        return ChatModelProviders.get(modelConfig.getImplClass())
                .createStream(modelConfig, parameter);
    }

    @Test
    void react_noTool_singleTurn() {
        TestContext ctx = new TestContext();
        engine.chat(createRequest("What is the capital of France?"), ctx.listener);

        System.out.println("\n\n=== TEST: No Tool ===");
        System.out.println("Tokens: " + ctx.tokens);

        assertNull(ctx.error, "Should not have error: " + (ctx.error != null ? ctx.error.getMessage() : ""));
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.tokens.length() > 0, "Should have tokens");
        assertEquals("", ctx.toolCalls.toString(), "Should not call any tools for simple factual question");
    }

    @Test
    void react_calculate_twoPlusTwo() {
        TestContext ctx = new TestContext();
        engine.chat(createRequest("What is 123 + 456?"), ctx.listener);

        System.out.println("\n\n=== TEST: Calculate ===");
        System.out.println("Tokens: " + ctx.tokens);
        System.out.println("Tool calls: " + ctx.toolCalls);
        System.out.println("Results: " + ctx.results);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.toolCalls.toString().contains("calculate"), "Should call calculate tool");
        assertTrue(ctx.results.toString().contains("579"), "Should have correct result");
    }

    @Test
    void react_weather() {
        TestContext ctx = new TestContext();
        engine.chat(createRequest("What's the weather like in Tokyo today?"), ctx.listener);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.toolCalls.toString().contains("get_weather"), "Should call get_weather tool");
    }

    @Test
    void react_time() {
        TestContext ctx = new TestContext();
        engine.chat(createRequest("What time is it now in Tokyo?"), ctx.listener);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.toolCalls.toString().contains("get_current_time"), "Should call get_current_time tool");
    }

    @Test
    void react_multipleTools() {
        TestContext ctx = new TestContext();
        engine.chat(createRequest("Calculate 50 + 50, then multiply the result by 2"), ctx.listener);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
    }

    @Test
    void react_translate() {
        TestContext ctx = new TestContext();
        engine.chat(createRequest("Translate 'Hello, how are you?' from English to Japanese"), ctx.listener);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.toolCalls.toString().contains("translate"), "Should call translate tool");
    }

    static class TestContext {
        StringBuilder tokens = new StringBuilder();
        StringBuilder toolCalls = new StringBuilder();
        StringBuilder results = new StringBuilder();
        boolean completed = false;
        Throwable error = null;

        StreamListener listener = new StreamListener() {
            @Override
            public void onThinkStart() {
                System.out.println("[THINK START]");
            }

            @Override
            public void onThinking(String thinking) {
            }

            @Override
            public void onThinkEnd() {
                System.out.println("[THINK END]");
            }

            @Override
            public void onToken(String token) {
                System.out.print("[TOKEN] " + token);
                if (token != null) {
                    tokens.append(token);
                }
            }

            @Override
            public void onToolCallStart(String toolId, String toolName, String arguments) {
                System.out.println("\n[TOOL START] " + toolName + " (id=" + toolId + ")");
                toolCalls.append(toolName).append(",");
            }

            @Override
            public void onToolCallEnd(ToolCall toolCall) {
                System.out.println("\n[TOOL END] " + toolCall.toolName() + " (id=" + toolCall.toolId() + ", args=" + toolCall.arguments() + ") -> " + toolCall.result());
                results.append(toolCall.result()).append(",");
            }

            @Override
            public void onComplete(String fullOutput) {
                System.out.println("\n[COMPLETE]");
                completed = true;
            }

            @Override
            public void onRoundComplete() {
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("\n[ERROR] " + e.getMessage());
                error = e;
            }
        };
    }

    static class TestNodeExecutor implements NodeExecutor {
        @Override
        public Object execute(String nodeId, Map<String, Object> args) {
            System.out.println("\n[EXECUTOR] Node: " + nodeId + " Args: " + args);

            return switch (nodeId) {
                case "calculate" -> executeCalculate(args);
                case "get_weather" -> executeGetWeather(args);
                case "get_current_time" -> executeGetCurrentTime(args);
                case "search" -> executeSearch(args);
                case "translate" -> executeTranslate(args);
                default -> "unknown_tool: " + nodeId;
            };
        }

        private Object executeCalculate(Map<String, Object> args) {
            int a = getInt(args, "a");
            int b = getInt(args, "b");
            String op = getString(args, "operation", "add");

            return switch (op.toLowerCase()) {
                case "add", "plus", "+" -> String.valueOf(a + b);
                case "subtract", "minus", "-" -> String.valueOf(a - b);
                case "multiply", "times", "*" -> String.valueOf(a * b);
                case "divide", "/", "\u00f7" ->
                    b != 0 ? String.valueOf((double) a / b) : "Error: division by zero";
                default -> "Error: unknown operation '" + op + "'";
            };
        }

        private Object executeGetWeather(Map<String, Object> args) {
            String city = getString(args, "city", "Unknown");
            return "{\"city\": \"%s\", \"temperature\": 22, \"condition\": \"Partly Cloudy\"}".formatted(city);
        }

        private Object executeGetCurrentTime(Map<String, Object> args) {
            String timezone = getString(args, "timezone", "UTC");
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return "{\"timezone\": \"%s\", \"time\": \"%s\"}".formatted(timezone, now.format(formatter));
        }

        private Object executeSearch(Map<String, Object> args) {
            String query = getString(args, "query", "");
            return "[{\"title\": \"Result for '%s'\", \"url\": \"https://example.com/1\"}]".formatted(query);
        }

        private Object executeTranslate(Map<String, Object> args) {
            String text = getString(args, "text", "");
            String from = getString(args, "from", "en");
            String to = getString(args, "to", "ja");
            return "{\"original\": \"%s\", \"from\": \"%s\", \"to\": \"%s\", \"translated\": \"[translated] %s\"}"
                    .formatted(text, from, to, text);
        }

        private int getInt(Map<String, Object> args, String key) {
            Object value = args.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
            return 0;
        }

        private String getString(Map<String, Object> args, String key, String defaultValue) {
            Object value = args.get(key);
            return value != null ? value.toString() : defaultValue;
        }
    }
}
