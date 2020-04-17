package org.jboss.weld.tests.extensions;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

@RailwayFurniture
public class SignalBox {

    @Produces
    public SignalMan getSignalMan() {
        return new SignalMan("David");
    }

    @Produces
    Mouse mouse = new Mouse("Whiskers");

    public void observe(@Observes Signals signals) {

    }

}
