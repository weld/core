package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

@AsyncHandler.ReturnType
@SuppressWarnings("rawtypes")
public class RawAsyncHandler implements AsyncHandler {
    @Override
    public Object transform(Object original, Runnable completion) {
        return original;
    }
}
