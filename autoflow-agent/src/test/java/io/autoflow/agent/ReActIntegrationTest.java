package io.autoflow.agent;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.autoflow.agent.memory.InMemoryMemoryStore;
import io.autoflow.agent.prompt.DefaultPromptTemplateProvider;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.provider.ChatModelProviders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private InMemoryMemoryStore memoryStore;

    @BeforeEach
    void setUp() {
        memoryStore = new InMemoryMemoryStore();
        ToolRegistry toolRegistry = createToolRegistry();

        StreamingChatModel streamingChatModel = createStreamingChatModel();

        engine = ReActAgent.builder()
                .memoryStore(memoryStore)
                .chatModel(streamingChatModel)
                .nodeExecutor(new TestNodeExecutor())
                .toolRegistry(toolRegistry)
                .maxSteps(10)
                .build();
    }

    private ToolRegistry createToolRegistry() {
        return new ToolRegistry() {
            @Override
            public List<ToolSpecification> getToolSpecifications() {
                return List.of(
                        // Calculator tool
                        ToolSpecification.builder()
                                .name("calculate")
                                .description("Perform basic arithmetic calculations. Input: two integers a and b, and operation (add/subtract/multiply/divide)")
                                .parameters(JsonObjectSchema.builder()
                                        .addIntegerProperty("a")
                                        .addIntegerProperty("b")
                                        .addStringProperty("operation")
                                        .build())
                                .build(),
                        // Weather tool
                        ToolSpecification.builder()
                                .name("get_weather")
                                .description("Get current weather for a city. Input: city name")
                                .parameters(JsonObjectSchema.builder()
                                        .addStringProperty("city")
                                        .build())
                                .build(),
                        // Time tool
                        ToolSpecification.builder()
                                .name("get_current_time")
                                .description("Get current time. Input: timezone (e.g., Asia/Shanghai)")
                                .parameters(JsonObjectSchema.builder()
                                        .addStringProperty("timezone")
                                        .build())
                                .build(),
                        // Search tool
                        ToolSpecification.builder()
                                .name("search")
                                .description("Search the web for information. Input: query string")
                                .parameters(JsonObjectSchema.builder()
                                        .addStringProperty("query")
                                        .build())
                                .build(),
                        // Translate tool
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
                return toolName; // Node ID is the tool name itself for test
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

    /**
     * Test 1: Direct answer without tool usage
     */
    @Test
    void react_noTool_singleTurn() {
        String sessionId = "test-no-tool-" + System.currentTimeMillis();
        TestContext ctx = new TestContext();

        AgentContext context = new AgentContext(sessionId);
        context.setSystemPrompt(new DefaultPromptTemplateProvider().getSystemPromptTemplate());
        memoryStore.save(context);

        engine.chat(sessionId, "What is the capital of France?", ctx.listener);

        System.out.println("\n\n=== TEST: No Tool ===");
        System.out.println("Tokens: " + ctx.tokens);
        System.out.println("Tool calls: " + ctx.toolCalls);
        System.out.println("Results: " + ctx.results);

        assertNull(ctx.error, "Should not have error: " + (ctx.error != null ? ctx.error.getMessage() : ""));
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.tokens.length() > 0, "Should have tokens");
        assertEquals("", ctx.toolCalls.toString(), "Should not call any tools for simple factual question");
    }

    /**
     * Test 2: Calculator tool usage
     */
    @Test
    void react_calculate_twoPlusTwo() {
        String sessionId = "test-calc-" + System.currentTimeMillis();
        TestContext ctx = new TestContext();

        AgentContext context = new AgentContext(sessionId);
        context.setSystemPrompt(new DefaultPromptTemplateProvider().getSystemPromptTemplate());
        memoryStore.save(context);

        engine.chat(sessionId, "What is 123 + 456?", ctx.listener);

        System.out.println("\n\n=== TEST: Calculate ===");
        System.out.println("Tokens: " + ctx.tokens);
        System.out.println("Tool calls: " + ctx.toolCalls);
        System.out.println("Results: " + ctx.results);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.toolCalls.toString().contains("calculate"), "Should call calculate tool");
        assertTrue(ctx.results.toString().contains("579"), "Should have correct result");
    }

    /**
     * Test 3: Weather tool usage
     */
    @Test
    void react_weather() {
        String sessionId = "test-weather-" + System.currentTimeMillis();
        TestContext ctx = new TestContext();

        AgentContext context = new AgentContext(sessionId);
        context.setSystemPrompt(new DefaultPromptTemplateProvider().getSystemPromptTemplate());
        memoryStore.save(context);

        engine.chat(sessionId, "What's the weather like in Tokyo today?", ctx.listener);

        System.out.println("\n\n=== TEST: Weather ===");
        System.out.println("Tokens: " + ctx.tokens);
        System.out.println("Tool calls: " + ctx.toolCalls);
        System.out.println("Results: " + ctx.results);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.toolCalls.toString().contains("get_weather"), "Should call get_weather tool");
    }

    /**
     * Test 4: Time tool usage
     */
    @Test
    void react_time() {
        String sessionId = "test-time-" + System.currentTimeMillis();
        TestContext ctx = new TestContext();

        AgentContext context = new AgentContext(sessionId);
        context.setSystemPrompt(new DefaultPromptTemplateProvider().getSystemPromptTemplate());
        memoryStore.save(context);

        engine.chat(sessionId, "What time is it now in Tokyo?", ctx.listener);

        System.out.println("\n\n=== TEST: Time ===");
        System.out.println("Tokens: " + ctx.tokens);
        System.out.println("Tool calls: " + ctx.toolCalls);
        System.out.println("Results: " + ctx.results);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
        assertTrue(ctx.toolCalls.toString().contains("get_current_time"), "Should call get_current_time tool");
    }

    /**
     * Test 5: Multiple tool calls in sequence
     */
    @Test
    void react_multipleTools() {
        String sessionId = "test-multi-" + System.currentTimeMillis();
        TestContext ctx = new TestContext();

        AgentContext context = new AgentContext(sessionId);
        context.setSystemPrompt(new DefaultPromptTemplateProvider().getSystemPromptTemplate());
        memoryStore.save(context);

        engine.chat(sessionId, "Calculate 50 + 50, then multiply the result by 2", ctx.listener);

        System.out.println("\n\n=== TEST: Multiple Tools ===");
        System.out.println("Tokens: " + ctx.tokens);
        System.out.println("Tool calls: " + ctx.toolCalls);
        System.out.println("Results: " + ctx.results);

        assertNull(ctx.error, "Should not have error");
        assertTrue(ctx.completed, "Should complete");
    }

    /**
     * Test 6: Translate tool usage
     */
    @Test
    void react_translate() {
        String sessionId = "test-translate-" + System.currentTimeMillis();
        TestContext ctx = new TestContext();

        AgentContext context = new AgentContext(sessionId);
        context.setSystemPrompt(new DefaultPromptTemplateProvider().getSystemPromptTemplate());
        memoryStore.save(context);

        engine.chat(sessionId, "Translate 'Hello, how are you?' from English to Japanese", ctx.listener);

        System.out.println("\n\n=== TEST: Translate ===");
        System.out.println("Tokens: " + ctx.tokens);
        System.out.println("Tool calls: " + ctx.toolCalls);
        System.out.println("Results: " + ctx.results);

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
            public void onThinking(String thinking) {
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
            public void onToolCallEnd(String toolId, String toolName, Object result) {
                System.out.println("\n[TOOL END] " + toolName + " (id=" + toolId + ") -> " + result);
                results.append(result).append(",");
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
                case "divide", "/", "÷" -> b != 0 ? String.valueOf((double) a / b) : "Error: division by zero";
                default -> "Error: unknown operation '" + op + "'";
            };
        }

        private Object executeGetWeather(Map<String, Object> args) {
            String city = getString(args, "city", "Unknown");
            // Mock weather data
            return """
                {"city": "%s", "temperature": 22, "condition": "Partly Cloudy", "humidity": 65, "wind": 12}
                """.formatted(city);
        }

        private Object executeGetCurrentTime(Map<String, Object> args) {
            String timezone = getString(args, "timezone", "UTC");
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return """
                {"timezone": "%s", "time": "%s", "formatted": "%s"}
                """.formatted(timezone, now.toString(), now.format(formatter));
        }

        private Object executeSearch(Map<String, Object> args) {
            String query = getString(args, "query", "");
            // Mock search results
            return """
                [{"title": "Result 1 for '%s'", "url": "https://example.com/1", "snippet": "This is a relevant result..."},
                 {"title": "Result 2 for '%s'", "url": "https://example.com/2", "snippet": "Another relevant result..."}]
                """.formatted(query, query);
        }

        private Object executeTranslate(Map<String, Object> args) {
            String text = getString(args, "text", "");
            String from = getString(args, "from", "en");
            String to = getString(args, "to", "ja");
            // Mock translation
            return """
                {"original": "%s", "from": "%s", "to": "%s", "translated": "[translated] %s"}
                """.formatted(text, from, to, text);
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
