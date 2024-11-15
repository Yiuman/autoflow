package io.autoflow.core.events;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yiuman
 * @date 2024/11/14
 */
@Slf4j
public final class EventDispatcherImpl implements EventDispatcher {
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public static EventDispatcher getDefault() {
        return Instance.EVENT_DISPATCHER;
    }

    @Override
    public void addEventListener(EventListener eventListener) {
        listeners.add(eventListener);
    }

    @Override
    public void dispatch(Event event) {
        for (EventListener listener : listeners) {
            try {
                boolean contains = listener.getTypes().stream()
                        .map(Enum::name)
                        .toList().contains(event.name());

                if (contains) {
                    listener.onEvent(event);
                }
            } catch (Throwable throwable) {
                log.warn("dispatch event happen error", throwable);
            }


        }
    }

    public static final class Instance {
        private static final EventDispatcher EVENT_DISPATCHER = new EventDispatcherImpl();
    }
}
