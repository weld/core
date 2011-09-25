package org.jboss.weld.tests.contexts;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

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
