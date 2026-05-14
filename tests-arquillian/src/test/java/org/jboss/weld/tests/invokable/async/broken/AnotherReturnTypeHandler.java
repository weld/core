package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

public class AnotherReturnTypeHandler<T> implements AsyncHandler.ReturnType<MyAsyncType<T>> {
    @Override
    public MyAsyncType<T> transform(MyAsyncType<T> original, Runnable completion) {
        return original;
    }
}
