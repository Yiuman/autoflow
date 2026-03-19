package io.autoflow.agent;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.StreamingChatModel;
import io.autoflow.agent.memory.InMemoryMemoryStore;
import io.autoflow.agent.parser.JsonActionParser;
import io.autoflow.plugin.llm.ModelConfig;
import io.autoflow.plugin.llm.provider.ChatModelProviders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for ReAct flow with real LLM.
 * Uses MiniMax API for actual streaming responses.
 */
class ReActIntegrationTest {

    private DefaultAgentEngine engine;
    private InMemoryMemoryStore memoryStore;
    private JsonActionParser actionParser;
    private ToolRegistry toolRegistry;
    private LangChainReasoner reasoner;

    @BeforeEach
    void setUp() {
        memoryStore = new InMemoryMemoryStore();
        actionParser = new JsonActionParser();
        toolRegistry = new ToolRegistry() {
            @Override
            public List<ToolSpecification> getToolSpecifications() {
                return List.of(ToolSpecification.builder()
                        .name("calculate")
                        .description("calculate")
                        .build());
            }

            @Override
            public String getNodeId(String toolName) {
                return "calculate";
            }
        };

        StreamingChatModel streamingChatModel = createStreamingChatModel();
        reasoner = new LangChainReasoner(streamingChatModel);

        engine = new DefaultAgentEngine(
                memoryStore,
                reasoner,
                actionParser,
                new TestNodeExecutor(),
                toolRegistry,
                5
        );
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
    void react_withRealModel_singleTurn() {
        String sessionId = "test-real-" + System.currentTimeMillis();
        String systemPrompt = """
                You are a helpful assistant. When you need to perform actions, respond with JSON in this format:
                {"action":"call_tool","tool":"tool_name","args":{"arg1":"value1"}}
                When finished, respond with:
                {"action":"finish"}
                """;

        AgentContext context = new AgentContext(sessionId);
        context.setSystemPrompt(systemPrompt);
        memoryStore.save(context);

        StringBuilder tokens = new StringBuilder();
        StringBuilder toolCalls = new StringBuilder();
        StringBuilder results = new StringBuilder();
        boolean[] completed = new boolean[]{false};
        Throwable[] error = new Throwable[1];

        StreamListener listener = new StreamListener() {
            @Override
            public void onToken(String token) {
                System.out.print(token);
                if (token != null) {
                    tokens.append(token);
                }
            }

            @Override
            public void onToolStart(String toolName) {
                System.out.println("\n[TOOL START] " + toolName);
                toolCalls.append(toolName).append(",");
            }

            @Override
            public void onToolEnd(String toolName, Object result) {
                System.out.println("\n[TOOL END] " + toolName + " -> " + result);
                results.append(result).append(",");
            }

            @Override
            public void onComplete() {
                System.out.println("\n[COMPLETE]");
                completed[0] = true;
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("\n[ERROR] " + e.getMessage());
                error[0] = e;
            }
        };

        engine.chat(sessionId, "Hello, what is 2+2?", listener);

        System.out.println("\n\n=== TEST RESULT ===");
        System.out.println("Tokens length: " + tokens.length());
        System.out.println("Tokens: " + tokens);
        System.out.println("Tool calls: " + toolCalls);
        System.out.println("Results: " + results);
        System.out.println("Completed: " + completed[0]);
        System.out.println("Error: " + (error[0] != null ? error[0].getMessage() : "none"));

        assertNull(error[0], "Should not have error: " + (error[0] != null ? error[0].getMessage() : ""));
        assertTrue(completed[0], "Should complete");
        assertTrue(tokens.length() > 0, "Should have tokens, got: " + tokens);
    }

    static class TestNodeExecutor implements NodeExecutor {
        @Override
        public Object execute(String nodeId, Map<String, Object> args) {
            if ("calculate".equals(nodeId)) {
                int a = (int) args.get("a");
                int b = (int) args.get("b");
                return String.valueOf(a + b);
            }
            return "unknown_tool";
        }
    }
}
