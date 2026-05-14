package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

public class TypeVariableAsyncHandler<T> implements AsyncHandler.ReturnType<T> {
    @Override
    public T transform(T original, Runnable completion) {
        return original;
    }
}
