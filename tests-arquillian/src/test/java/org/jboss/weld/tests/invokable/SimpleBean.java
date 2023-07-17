package org.jboss.weld.tests.invokable;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.invoke.Invokable;

@Dependent
public class SimpleBean {

    public static int preDestroyInvoked = 0;

    @Invokable
    public String ping(String s, int i) {
        return s + i;
    }

    @Invokable
    public static String staticPing(String s, int i) {
        return s + i;
    }

    @PreDestroy
    public void destroy() {
        preDestroyInvoked++;
    }

    public static void resetDestroyCounter() {
        preDestroyInvoked = 0;
    }

}
