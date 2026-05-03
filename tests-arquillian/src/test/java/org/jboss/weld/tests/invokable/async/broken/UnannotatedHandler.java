package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

// With the new API, a handler must implement either ReturnType or ParameterType.
// This class implements neither — it is not a valid handler.
// TODO: revisit this test with the new async handler API
public class UnannotatedHandler<T> implements AsyncHandler.ReturnType<T> {
    @Override
    public T transform(T original, Runnable completion) {
        return original;
    }
}
