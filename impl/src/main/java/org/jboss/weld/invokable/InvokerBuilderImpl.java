package org.jboss.weld.invokable;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.invoke.Invoker;

import java.lang.reflect.Method;

public class InvokerBuilderImpl<T> extends AbstractInvokerBuilder {

    public InvokerBuilderImpl(Class<T> beanClass, Method method, BeanManager beanManager) {
        super(beanClass, method, beanManager);
    }

    @Override
    public Invoker<T, ?> build() {
        return new InvokerImpl<>(this);
    }
}
