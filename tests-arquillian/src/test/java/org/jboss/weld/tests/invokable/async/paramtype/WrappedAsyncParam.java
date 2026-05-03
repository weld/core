package org.jboss.weld.tests.invokable.async.paramtype;

public class WrappedAsyncParam<T> implements MyAsyncParam<T> {
    private final MyAsyncParam<T> delegate;

    WrappedAsyncParam(MyAsyncParam<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isComplete() {
        return delegate.isComplete();
    }

    @Override
    public T getIfComplete() {
        return delegate.getIfComplete();
    }

    @Override
    public void whenComplete(Runnable callback) {
        delegate.whenComplete(callback);
    }

    @Override
    public void resume(T value) {
        delegate.resume(value);
    }
}
