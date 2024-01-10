package org.jboss.weld.tests.invokable.common;

import jakarta.enterprise.invoke.Invoker;

public class InvocationWrapper {
    public static Object transform(SimpleBean instance, Object[] arguments, Invoker<SimpleBean, Object> invoker)
            throws Exception {
        // perform repeated invocation; just to verify the invoker is stateless
        Object result1 = invoker.invoke(instance, arguments);
        Object result2 = invoker.invoke(instance, arguments);
        if (result1 instanceof String && result2 instanceof String) {
            return (String) result1 + result2;
        } else {
            throw new AssertionError();
        }
    }
}
