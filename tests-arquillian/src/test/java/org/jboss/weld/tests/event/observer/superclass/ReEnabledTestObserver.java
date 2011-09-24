package org.jboss.weld.tests.event.observer.superclass;

import javax.enterprise.event.Observes;

@ReEnabled
public class ReEnabledTestObserver extends DisabledTestObserver {
    @Override
        // reenables observation by overriding and adding @Observes
    void observe(@Observes TestEvent event) {
        super.observe(event);
    }
}
