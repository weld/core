package org.jboss.weld.tests.event.ordering;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;

public class Bravo {

    public void observeEvent(@Observes @Priority(100) EventPayload payload) {
        payload.record(Bravo.class.getName());
    }

}
