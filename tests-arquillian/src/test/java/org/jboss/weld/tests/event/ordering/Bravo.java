package org.jboss.weld.tests.event.ordering;

import javax.enterprise.event.Observes;

import org.jboss.weld.experimental.Priority;

public class Bravo {

    public void observeEvent(@Observes @Priority(100) EventPayload payload) {
        payload.record(Bravo.class.getName());
    }

}
