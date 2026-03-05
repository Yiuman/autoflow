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
 * 工具调用记录
 *
 * @author autoflow
 * @date 2025/03/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_tool_call")
public class ToolCall extends BaseEntity<String> {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;
    /**
     * 消息ID
     */
    private String messageId;
    /**
     * 工具名称
     */
    private String toolName;
    /**
     * 参数（JSON格式）
     */
    private String parameters;
    /**
     * 结果（JSON格式）
     */
    private String result;
    /**
     * 状态
     */
    private String status;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
