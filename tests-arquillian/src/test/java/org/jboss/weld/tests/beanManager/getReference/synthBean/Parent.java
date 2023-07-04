package org.jboss.weld.tests.beanManager.getReference.synthBean;

public class Parent {

    private final Child child;

    Parent(final Child child) {
        super();
        this.child = child;
    }
}
