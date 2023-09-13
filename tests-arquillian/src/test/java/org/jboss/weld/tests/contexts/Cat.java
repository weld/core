package org.jboss.weld.tests.contexts;

import static jakarta.enterprise.event.Reception.IF_EXISTS;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;

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
