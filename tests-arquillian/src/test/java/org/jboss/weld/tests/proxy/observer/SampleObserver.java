package org.jboss.weld.tests.proxy.observer;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;


@SessionScoped
public class SampleObserver implements Serializable {

    private static final long serialVersionUID = -8311790045944819159L;

    @Inject
    private Qux qux;

    private boolean injectionAndObservationOccured;

    @SuppressWarnings("unused")
    private void observes(@Observes Baz baz) {
        injectionAndObservationOccured = qux != null;
    }

    public boolean isInjectionAndObservationOccured() {
        return injectionAndObservationOccured;
    }

}
