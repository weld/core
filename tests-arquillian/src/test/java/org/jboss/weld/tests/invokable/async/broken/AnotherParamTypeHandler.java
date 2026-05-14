package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

public class AnotherParamTypeHandler<T> implements AsyncHandler.ParameterType<MyAsyncType<T>> {
    @Override
    public MyAsyncType<T> transformArgument(MyAsyncType<T> original, Runnable completion) {
        return original;
    }
}
