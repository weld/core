package org.jboss.weld.tests.invokable.common;

// servers as transformable arg of a bean method
public class FooArg {

    private String s;

    public FooArg(String s) {
        this.s = s;
    }

    public FooArg doubleTheString() {
        this.s = s + s;
        return this;
    }

    public String getString() {
        return s;
    }
}
