package org.jboss.weld.environment.se.test.provider.custom;

public class MyException extends RuntimeException {

    private static final long serialVersionUID = 1365849814894651L;

    public MyException(String string) {
        super(string);
    }
}
