package org.jboss.weld.tests.producer.alternative.priority.complex;

public class Foo {

    private String s;

    public Foo(String s) {
        this.s = s;
    }

    public String ping() {
        return s;
    }
}
