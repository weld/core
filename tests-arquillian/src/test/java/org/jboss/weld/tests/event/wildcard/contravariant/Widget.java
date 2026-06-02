package org.jboss.weld.tests.event.wildcard.contravariant;

public class Widget {

    private final String name;

    public Widget(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
