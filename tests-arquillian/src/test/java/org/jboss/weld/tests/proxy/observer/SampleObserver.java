package org.jboss.weld.tests.proxy.observer;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

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
