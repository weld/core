package org.jboss.weld.invokable;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.invoke.Invoker;

import java.lang.invoke.MethodHandle;

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
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            // TODO at this point there might have been exception transformer invoked as well, guess we just rethrow?
            // we just rethrow the original exception
            throw new RuntimeException(e);
        }
    }
}
