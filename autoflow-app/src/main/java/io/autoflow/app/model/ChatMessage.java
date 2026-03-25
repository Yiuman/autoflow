package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_chat_message")
public class ChatMessage extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    private String sessionId;
    private String conversationId;
    private String role;
    private String content;
    private String thinkingContent;
    private String metadata;
}
