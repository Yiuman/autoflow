package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天会话
 *
 * @author autoflow
 * @date 2025/03/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_chat_session")
public class ChatSession extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    /**
     * 会话标题
     */
    private String title;
    /**
     * 会话状态
     */
    private String status;
}
