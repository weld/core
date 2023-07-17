package org.jboss.weld.tests.invokable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.invoke.Invokable;

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

    @Invokable
    public String ping(FooArg foo, String s) {
        String result = foo.getString() + s;
        if (transformed) {
            return result.toUpperCase();
        } else {
            return result;
        }
    }

    @Invokable
    public static String staticPing(FooArg foo, String s) {
        return foo.getString() + s;
    }
}
