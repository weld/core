package org.jboss.weld.tests.event.observer.superclass;

import javax.enterprise.event.Observes;

public abstract class AbstractTestObserver {
    private TestEvent testEvent;

    public TestEvent getTestEvent() {
        return testEvent;
    }

    void observe(@Observes TestEvent event) {
        this.testEvent = event;
    }

    public void reset() {
        testEvent = null;
    }
}
