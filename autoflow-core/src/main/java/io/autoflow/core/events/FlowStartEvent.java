package io.autoflow.core.events;

import io.autoflow.core.enums.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FlowStartEvent extends BaseFlowEvent {

    @Override
    public EventType getType() {
        return EventType.FlOW_START;
    }
}
