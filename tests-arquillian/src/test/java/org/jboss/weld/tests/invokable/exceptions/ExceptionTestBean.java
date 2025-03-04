package org.jboss.weld.tests.invokable.exceptions;

import jakarta.enterprise.context.Dependent;

@Dependent
public class ExceptionTestBean {

    public String ping(String s, int i) {
        return s + i;
    }

    public static String staticPing(String s, int i) {
        return s + i;
    }

    public void voidPing(String s, int i) {
        String result = s + i;
    }

    public String noargPing() {
        return "42";
    }
}
