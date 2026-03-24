package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI Model entity
 *
 * @author system
 * @date 2024/5/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_model")
public class Model extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    private String name;
    private String baseUrl;
    private String apiKey;
    private String config;
}
