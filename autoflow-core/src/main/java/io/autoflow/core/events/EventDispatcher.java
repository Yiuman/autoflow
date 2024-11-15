package io.autoflow.core.events;

/**
 * @author yiuman
 * @date 2024/11/14
 */
public interface EventDispatcher {

    void addEventListener(EventListener eventListener);

    void dispatch(Event event);
}