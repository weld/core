package org.jboss.weld.tests.observers.extension.configure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class ObservingBean {

    public static int timesObserved = 0;

    public void observe(@Observes Foo foo) {
        timesObserved++;
    }

    public void ping() {
        // no-op but forces bean init when invoked
    }
}
