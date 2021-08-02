package org.jboss.weld.tests.extensions.annotatedType.withAnnotations;


import jakarta.enterprise.context.Dependent;

import java.beans.ConstructorProperties;

@Dependent
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
