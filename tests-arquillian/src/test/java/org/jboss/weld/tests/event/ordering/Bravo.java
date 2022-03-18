package org.jboss.weld.tests.event.ordering;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;

@Dependent
public class Bravo {

    public void observeEvent(@Observes @Priority(100) EventPayload payload) {
        payload.record(Bravo.class.getName());
    }

}
