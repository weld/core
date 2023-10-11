package org.jboss.weld.tests.invokable.common;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TrulyExceptionalBean {

    public String ping(String something, int somethingElse) {
        throw new IllegalArgumentException("intended");
    }

    public static String staticPing(String something, int somethingElse) {
        throw new IllegalStateException("intended");
    }
}
