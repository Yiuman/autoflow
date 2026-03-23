package io.autoflow.agent;

import dev.langchain4j.agent.tool.ToolExecutionRequest;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ToolRetryHandler {

    private final int maxRetries;
    private final ConcurrentHashMap<String, Integer> failureCounts = new ConcurrentHashMap<>();

    public ToolRetryHandler(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean shouldRetry(String toolName, String args, String error) {
        String key = toolName + ":" + args;
        int failures = failureCounts.getOrDefault(key, 0);
        return failures < maxRetries;
    }

    public String buildReflectionMessage(String toolName, String error, int remaining) {
        return """
            Tool Execution Failed: %s
            Error: %s

            Reflection: Analyze what went wrong and plan your next action.
            - Was the tool called with correct arguments?
            - Is there a different tool that could achieve the same goal?
            - Should you try different parameters?

            You have %d retry(s) remaining.
            """.formatted(toolName, error, remaining);
    }

    public String buildFinalErrorMessage(String toolName, String error) {
        return """
            Tool Execution Failed: %s
            Error: %s

            This tool has failed after maximum retries.
            Please provide your best effort answer or explain that the task could not be completed.
            """.formatted(toolName, error);
    }

    public void recordFailure(String toolName, String args) {
        String key = toolName + ":" + args;
        failureCounts.compute(key, (k, v) -> v == null ? 1 : v + 1);
    }

    public void recordSuccess(String toolName, String args) {
        String key = toolName + ":" + args;
        failureCounts.remove(key);
    }

    public void clear() {
        failureCounts.clear();
    }

    public int getRemainingRetries(String toolName, String args) {
        String key = toolName + ":" + args;
        return maxRetries - failureCounts.getOrDefault(key, 0);
    }

    public List<ToolExecutionRequest> handleToolError(
            String toolName,
            String args,
            String errorMsg,
            List<ToolExecutionRequest> retryList,
            ToolExecutionRequest request) {
        
        if (shouldRetry(toolName, args, errorMsg)) {
            recordFailure(toolName, args);
            int remaining = getRemainingRetries(toolName, args);
            String reflection = buildReflectionMessage(toolName, errorMsg, remaining);
            retryList.add(request);
            return retryList;
        } else {
            return retryList;
        }
    }
}
