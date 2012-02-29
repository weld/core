package org.jboss.weld.tests.annotatedType.superclass;

import javax.inject.Inject;

/**
 * @author Gert Palok
 */
public class Parent {
    @Inject
    private Foo foo;

    public Foo getFoo() {
        return foo;
    }
}
