package org.jboss.weld.tests.event.ordering;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;

public class Charlie {

    public void observeEvent(@Observes @Priority(50) EventPayload payload) {
        payload.record(Charlie.class.getName());
    }

}
