package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局变量
 *
 * @author yiuman
 * @date 2024/4/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_global_var")
public class GlobalVariable extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    private String key;
    private String value;
    private String description;
}
