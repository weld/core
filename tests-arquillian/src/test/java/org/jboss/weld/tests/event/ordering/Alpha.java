package org.jboss.weld.tests.event.ordering;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;

@Dependent
public class Alpha {

    public void observeEvent(@Observes @Priority(1) EventPayload payload) {
        payload.record(Alpha.class.getName());
    }

}
