package org.jboss.weld.tests.invokable.common;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

@Dependent
public class SimpleBean {

    public static int preDestroyInvoked = 0;

    public String ping(String s, int i) {
        return s + i;
    }

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
