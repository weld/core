package org.jboss.weld.tests.proxy.superclass;

public class NotSimpleConstructorClass {


    public NotSimpleConstructorClass(String value) {
    }
    
    protected String giveMeNothing() {
        // dummy method
        return "nothing";
    }
}
