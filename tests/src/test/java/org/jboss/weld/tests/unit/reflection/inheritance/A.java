package org.jboss.weld.tests.unit.reflection.inheritance;

/**
 *
 */
public class A<E1, E2> {

    private Faz faz;

    private E1 e1;

    private Foo<E1> foo1;

    private Foo<E2> foo2;

    private Foo<String> stringFoo;

    private Foo<Foo<String>> stringFooFoo;

    private Foo<Foo<E1>> variableFooFoo;

    private Foo<String>[] stringFooArray;

    private E1[] variableArray;

    private E1[][] twoDimensionalVariableArray;

    private Foo<E1>[] foo1Array;
}
