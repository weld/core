package org.jboss.weld.tests.instance.wildcard.covariant;

import jakarta.enterprise.context.Dependent;

@Dependent
public class Widget {

    private final String name;

    public Widget() {
        this.name = "default";
    }

    public String getName() {
        return name;
    }
}
