package org.jboss.weld.tests.unit.reflection.inheritance;

/**
 *
 */
public class C<U, T> extends B<T, U> { // note the flipped arguments!

    private Foo<U> foo4;
}
