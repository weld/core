package org.jboss.weld.tests.contexts;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;

@SessionScoped
public class House implements Serializable {

    private Mouse mouse;

    public Mouse getMouse() {
        return mouse;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

}
