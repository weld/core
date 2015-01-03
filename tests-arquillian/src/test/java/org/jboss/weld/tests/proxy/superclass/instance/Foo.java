package org.jboss.weld.tests.proxy.superclass.instance;

/**
 * @author Yann Diorcet
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
