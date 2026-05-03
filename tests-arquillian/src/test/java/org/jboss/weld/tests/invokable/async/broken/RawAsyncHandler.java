package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

@SuppressWarnings("rawtypes")
public class RawAsyncHandler implements AsyncHandler.ReturnType {
    @Override
    public Object transform(Object original, Runnable completion) {
        return original;
    }
}
