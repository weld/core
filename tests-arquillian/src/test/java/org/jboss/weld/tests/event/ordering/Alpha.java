package org.jboss.weld.tests.event.ordering;

import javax.enterprise.event.Observes;

import org.jboss.weld.experimental.Priority;

public class Alpha {

    public void observeEvent(@Observes @Priority(1) EventPayload payload) {
        payload.record(Alpha.class.getName());
    }

}
