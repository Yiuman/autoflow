package io.autoflow.app.dto;

import lombok.Data;

import java.util.List;

/**
 * 聊天响应
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
public class ChatResponse {
    /**
     * 消息内容
     */
    private String message;

    /**
     * 工具调用列表
     */
    private List<ToolCallInfo> toolCalls;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 工具调用信息
     */
    @Data
    public static class ToolCallInfo {
        private String toolName;
        private String parameters;
        private String result;
    }
}
