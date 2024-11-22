package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 执行实例（对应每次Service的执行）
 *
 * @author yiuman
 * @date 2024/11/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_execution_inst")
public class ExecutionInst extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    /**
     * 流程定义的ID
     */
    private String workflowId;
    /**
     * 流程实例的ID
     */
    private String workflowInstId;
    /**
     * 节点ID
     */
    private String nodeId;
    /**
     * 服务插件ID
     */
    private String serviceId;
    /**
     * 循环ID
     */
    private String loopId;
    /**
     * 当前执行在本次循环中的下标
     */
    private Integer loopCounter;
    /**
     * 当前循环的数量
     */
    private Integer nrOfInstances;
    /**
     * 数据
     */
    private String data;
    /**
     * 起始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 耗时（单位毫秒）
     */
    private Long durationMs;
    /**
     * 执行的错误信息
     */
    private String errorMessage;
}
