package org.jboss.weld.tests.event.wildcard.contravariant;

public class LifecycleEvent<T> {

    private final T payload;

    public LifecycleEvent(T payload) {
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }
}
