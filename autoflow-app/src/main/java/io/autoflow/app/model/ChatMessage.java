package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天消息
 *
 * @author autoflow
 * @date 2025/03/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_chat_message")
public class ChatMessage extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 角色（user/assistant/system）
     */
    private String role;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 消息类型（message/token/tool_call/tool_result）
     */
    private String type;
    /**
     * 元数据（JSON格式，存储toolName/parameters/result/status等）
     */
    private String metadata;
}
