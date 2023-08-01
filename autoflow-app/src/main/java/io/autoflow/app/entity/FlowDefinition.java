package io.autoflow.app.entity;

import com.mybatisflex.annotation.Table;
import io.autoflow.common.crud.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程定义
 *
 * @author yiuman
 * @date 2023/7/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_flow_def")
public class FlowDefinition extends BaseEntity<String> {
    private String name;
    @NotBlank
    private String flowJson;
    private String processDefinitionId;
    private String processDefinitionKey;
}
