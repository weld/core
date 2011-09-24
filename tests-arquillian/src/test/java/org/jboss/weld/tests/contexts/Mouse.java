package org.jboss.weld.tests.contexts;

import java.io.Serializable;

public class Mouse implements Serializable {

    private final String name;

    public Mouse(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
