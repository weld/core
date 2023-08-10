package org.jboss.weld.tests.invokable.common;

// non-bean class intentionally
public class ReturnValueTransformer {

    static public String transform(String returnValue) {
        return returnValue.strip();
    }
}
