package io.autoflow.app.dto;

import lombok.Data;

/**
 * 聊天请求
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
public class ChatRequest {
    /**
     * 会话ID（可选，不传则创建新会话）
     */
    private String sessionId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 提供者（可选）
     */
    private String provider;
}
