package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

public class BothAnnotationsHandler<T> implements AsyncHandler.ReturnType<T>, AsyncHandler.ParameterType<T> {
    @Override
    public T transform(T original, Runnable completion) {
        return original;
    }

    @Override
    public T transformArgument(T original, Runnable completion) {
        return original;
    }
}
