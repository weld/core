package org.jboss.weld.tests.event.observer.superclass;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ReEnabled
@ApplicationScoped
public class ReEnabledTestObserver extends DisabledTestObserver {
    @Override
    // reenables observation by overriding and adding @Observes
    void observe(@Observes TestEvent event) {
        super.observe(event);
    }
}
