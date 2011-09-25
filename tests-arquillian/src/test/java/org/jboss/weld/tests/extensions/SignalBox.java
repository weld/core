package org.jboss.weld.tests.extensions;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

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
