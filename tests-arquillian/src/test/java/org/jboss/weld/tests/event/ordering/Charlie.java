package org.jboss.weld.tests.event.ordering;

import javax.enterprise.event.Observes;

import org.jboss.weld.experimental.Priority;

public class Charlie {

    public void observeEvent(@Observes @Priority(50) EventPayload payload) {
        payload.record(Charlie.class.getName());
    }

}
