package org.jboss.weld.tests.contexts;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;

import static jakarta.enterprise.event.Reception.IF_EXISTS;

@RequestScoped
public class Cat {

    private Mouse mouse;

    public void observe(@Observes(notifyObserver = IF_EXISTS) Mouse mouse) {
        this.mouse = mouse;
    }

    public Mouse getMouse() {
        return mouse;
    }

}
