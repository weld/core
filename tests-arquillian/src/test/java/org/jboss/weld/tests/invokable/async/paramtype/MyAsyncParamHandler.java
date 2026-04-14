package org.jboss.weld.tests.invokable.async.paramtype;

import jakarta.enterprise.invoke.AsyncHandler;

@AsyncHandler.ParameterType
public class MyAsyncParamHandler<T> implements AsyncHandler<MyAsyncParam<T>> {
    @Override
    public MyAsyncParam<T> transform(MyAsyncParam<T> original, Runnable completion) {
        original.whenComplete(completion);
        return original;
    }
}
