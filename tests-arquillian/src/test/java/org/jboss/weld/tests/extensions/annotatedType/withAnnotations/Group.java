package org.jboss.weld.tests.extensions.annotatedType.withAnnotations;


import java.beans.ConstructorProperties;

public class Group {

    private final String name;

    @ConstructorProperties({"name"})
    Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
