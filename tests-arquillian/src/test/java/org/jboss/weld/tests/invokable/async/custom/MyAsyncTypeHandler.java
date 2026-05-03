package org.jboss.weld.tests.invokable.async.custom;

import jakarta.enterprise.invoke.AsyncHandler;

public class MyAsyncTypeHandler<T> implements AsyncHandler.ReturnType<MyAsyncType<T>> {
    @Override
    public MyAsyncType<T> transform(MyAsyncType<T> original, Runnable completion) {
        return original.whenComplete(completion);
    }
}
