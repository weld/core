package org.jboss.weld.tests.invokable.common;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransformableBean {

    // used with instance transformer
    private boolean transformed = false;

    public TransformableBean setTransformed() {
        this.transformed = true;
        return this;
    }

    public boolean isTransformed() {
        return transformed;
    }

    public String ping(FooArg foo, String s) {
        String result = foo.getString() + s;
        if (transformed) {
            return result.toUpperCase();
        } else {
            return result;
        }
    }

    public static String staticPing(FooArg foo, String s) {
        return foo.getString() + s;
    }
}
