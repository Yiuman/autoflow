package io.autoflow.core.events;

import io.autoflow.core.model.Flow;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BaseFlowEvent extends AbstractEvent {
    private String flowId;
    private String flowInstId;
    private Flow flow;
}
