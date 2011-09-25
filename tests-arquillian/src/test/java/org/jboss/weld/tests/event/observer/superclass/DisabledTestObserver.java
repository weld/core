package org.jboss.weld.tests.event.observer.superclass;

@Disabled
public class DisabledTestObserver extends TestObserver {

    @Override
        // observation disabled because this overrides the observer method without
        // the @Observes
    void observe(TestEvent event) {
        super.observe(event);
    }
}
