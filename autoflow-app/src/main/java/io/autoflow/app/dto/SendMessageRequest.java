package io.autoflow.app.dto;

import lombok.Data;

/**
 * 发送消息请求
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
public class SendMessageRequest {
    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 提供者（可选）
     */
    private String provider;
}
