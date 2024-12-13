package io.autoflow.core.events;

import io.autoflow.core.enums.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/12/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FlowErrorEvent extends BaseFlowEvent {

    @Override
    public EventType getType() {
        return EventType.FLOW_END;
    }
}
