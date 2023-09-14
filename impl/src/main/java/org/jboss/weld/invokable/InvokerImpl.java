package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.invoke.Invoker;

public class InvokerImpl<T, R> implements Invoker<T, R>, InvokerInfo {
    private final MethodHandle mh;

    InvokerImpl(MethodHandle mh) {
        this.mh = mh;
    }

    @Override
    public R invoke(T instance, Object[] arguments) {
        try {
            return (R) mh.invoke(instance, arguments);
        } catch (ValueCarryingException e) {
            // exception transformer may return a value by throwing a special exception
            return (R) e.getMethodReturnValue();
        } catch (Throwable e) {
            throw SneakyThrow.sneakyThrow(e);
        }
    }
}
