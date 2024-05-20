package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.groups.Save;
import io.ola.crud.model.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

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
    @NotBlank(groups = Save.class)
    private String name;
    private String flowStr;
    private List<String> tags;
    private List<String> plugins;
    private String desc;
    private Integer version = 1;
}
