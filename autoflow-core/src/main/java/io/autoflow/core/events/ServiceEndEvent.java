package io.autoflow.core.events;

import io.autoflow.core.enums.EventType;
import io.autoflow.spi.model.ExecutionResult;
import io.autoflow.spi.model.ServiceData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceEndEvent extends AbstractEvent {
    private ServiceData serviceData;
    private ExecutionResult<?> executionResult;

    @Override
    public EventType getType() {
        return EventType.SERVICE_END;
    }
}
