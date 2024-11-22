package io.autoflow.app.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.groups.Save;
import io.ola.crud.model.BaseEntity;
import io.ola.crud.utils.ListTypeHandler;
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
@Table("af_workflow")
public class Workflow extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    @NotBlank(groups = Save.class)
    private String name;
    private String flowStr;
    @Column(typeHandler = ListTypeHandler.class)
    private List<String> tagIds;
    @Column(typeHandler = ListTypeHandler.class)
    private List<String> pluginIds;
    private String description;
    private Integer version = 1;
}
