package org.jboss.weld.tests.event.wildcard.contravariant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class WidgetObserver {

    private boolean observed = false;

    public void onWidget(@Observes Widget event) {
        observed = true;
    }

    public boolean isObserved() {
        return observed;
    }
}
