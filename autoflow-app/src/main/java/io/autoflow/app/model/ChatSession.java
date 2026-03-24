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
@Table("af_chat_session")
public class ChatSession extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    private String title;
    private String modelId;
    private String systemPrompt;
    private String status;
    private String creator;
}
