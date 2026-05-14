package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

public class BothInterfacesHandler<T>
        implements AsyncHandler.ReturnType<MyAsyncType<T>>, AsyncHandler.ParameterType<MyAsyncType<T>> {
    @Override
    public MyAsyncType<T> transform(MyAsyncType<T> original, Runnable completion) {
        return original;
    }

    @Override
    public MyAsyncType<T> transformArgument(MyAsyncType<T> original, Runnable completion) {
        return original;
    }
}
