package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

@AsyncHandler.ReturnType
@AsyncHandler.ParameterType
public class BothAnnotationsHandler<T> implements AsyncHandler<T> {
    @Override
    public T transform(T original, Runnable completion) {
        return original;
    }
}
