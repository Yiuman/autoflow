package io.autoflow.app.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.ola.crud.model.BaseEntity;
import io.ola.crud.utils.ListTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Agent configuration entity for storing system prompts and agent settings.
 *
 * @author yiuman
 * @date 2024/12/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_agent_config")
public class AgentConfig extends BaseEntity<String> {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;

    /**
     * Configuration name, e.g., "General Assistant", "Workflow Designer"
     */
    private String name;

    /**
     * System prompt for the agent
     */
    private String systemPrompt;

    /**
     * Maximum steps for ReAct agent execution
     */
    private Integer maxSteps;

    /**
     * Maximum tool retry attempts
     */
    private Integer maxToolRetries;

    /**
     * Enabled tool IDs for this agent configuration.
     * If null or empty, all tools are enabled.
     */
    @Column(typeHandler = ListTypeHandler.class)
    private List<String> toolIds;
}
