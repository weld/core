package org.jboss.weld.tests.weld1827;

import javax.enterprise.context.Dependent;

/**
 *
 */
public class Foo {

    private String name;

    public Foo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
