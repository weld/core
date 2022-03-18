package org.jboss.weld.tests.event.ordering;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;

@Dependent
public class NoPriority {

    public void observeEvent(@Observes EventPayload payload) {
        payload.record(NoPriority.class.getName());
    }

}
