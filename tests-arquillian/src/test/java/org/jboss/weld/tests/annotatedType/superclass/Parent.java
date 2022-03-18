package org.jboss.weld.tests.annotatedType.superclass;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * @author Gert Palok
 */
@Dependent
public class Parent {
    @Inject
    private Foo foo;

    public Foo getFoo() {
        return foo;
    }
}
