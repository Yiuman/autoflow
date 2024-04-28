package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流
 *
 * @author yiuman
 * @date 2024/4/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("AF_WORKFLOW")
public class Workflow extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    private String name;
    private String flowStr;
    private String desc;
}
