package io.autoflow.core.events;

import io.autoflow.core.enums.EventType;
import io.autoflow.spi.context.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yiuman
 * @date 2024/11/14
 */
public abstract class AbstractEvent implements Event {

    @Getter
    @Setter
    private ExecutionContext context;

    public abstract EventType getType();

    @Override
    public String name() {
        return getType().name();
    }
}
