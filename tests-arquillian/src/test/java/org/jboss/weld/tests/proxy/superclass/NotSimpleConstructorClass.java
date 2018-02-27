package org.jboss.weld.tests.proxy.superclass;

public class NotSimpleConstructorClass {

    private String value;

    public NotSimpleConstructorClass(String value) {
        this.value = value;
    }

    protected String giveMeNothing() {
        // dummy method
        return "nothing";
    }

    public String getValue() {
        return value;
    }

}