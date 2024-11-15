package io.autoflow.core.events;

import io.autoflow.core.enums.EventType;

import java.util.Collection;

/**
 * @author yiuman
 * @date 2024/11/14
 */
public interface EventListener {

    void onEvent(Event event);

    Collection<? extends EventType> getTypes();

}