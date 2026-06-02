package org.jboss.weld.tests.event.wildcard.contravariant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithContravariantEvent {

    @Inject
    Event<? super LifecycleEvent<?>> lifecycleEvents;

    public void fireEvent(LifecycleEvent<?> event) {
        lifecycleEvents.fire(event);
    }
}
