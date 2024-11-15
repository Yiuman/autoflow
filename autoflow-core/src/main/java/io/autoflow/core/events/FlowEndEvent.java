package io.autoflow.core.events;

import io.autoflow.core.enums.EventType;
import io.autoflow.spi.model.FlowExecutionResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FlowEndEvent extends BaseFlowEvent {
    private FlowExecutionResult flowExecutionResult;

    @Override
    public EventType getType() {
        return EventType.FLOW_END;
    }
}
