package org.jboss.weld.tests.invokable.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.invoke.Invokable;

@ApplicationScoped
public class TrulyExceptionalBean {

    @Invokable
    public String ping(String something, int somethingElse) {
        throw new IllegalArgumentException("intended");
    }

    @Invokable
    public static String staticPing(String something, int somethingElse) {
        throw new IllegalStateException("intended");
    }
}
