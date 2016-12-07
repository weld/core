package org.jboss.weld.tests.event.ordering;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;

public class Alpha {

    public void observeEvent(@Observes @Priority(1) EventPayload payload) {
        payload.record(Alpha.class.getName());
    }

}
