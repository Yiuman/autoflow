package io.autoflow.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("af_flow_def")
public class FlowDefinition extends BaseEntity<String> {
    private String flowJson;
    private String processDefinitionId;
    private String processDefinitionKey;
}
