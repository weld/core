package org.jboss.weld.tests.invokable.async.paramtype;

import jakarta.enterprise.invoke.AsyncHandler;

public class MyAsyncParamHandler<T> implements AsyncHandler.ParameterType<MyAsyncParam<T>> {
    @Override
    public MyAsyncParam<T> transformArgument(MyAsyncParam<T> original, Runnable completion) {
        original.whenComplete(completion);
        return original;
    }
}
