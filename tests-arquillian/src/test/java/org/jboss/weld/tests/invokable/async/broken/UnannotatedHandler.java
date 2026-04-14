package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

// Missing both @ReturnType and @ParameterType
public class UnannotatedHandler<T> implements AsyncHandler<T> {
    @Override
    public T transform(T original, Runnable completion) {
        return original;
    }
}
