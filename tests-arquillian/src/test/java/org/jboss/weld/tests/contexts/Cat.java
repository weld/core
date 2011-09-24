package org.jboss.weld.tests.contexts;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;

import static javax.enterprise.event.Reception.IF_EXISTS;

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
