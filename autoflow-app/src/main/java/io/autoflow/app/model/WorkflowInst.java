package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.autoflow.app.enums.FlowState;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author yiuman
 * @date 2024/11/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("AF_WORKFLOW_INST")
public class WorkflowInst extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    /**
     * 流程定义ID
     */
    private String workflowId;
    /**
     * 流程启动时间
     */
    private LocalDateTime startTime;
    /**
     * 流程结束时间
     */
    private LocalDateTime endTime;
    /**
     * 耗时
     */
    private Long durationMs;
    /**
     * 流程状态
     */
    private FlowState flowState;
}
