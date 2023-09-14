package org.jboss.weld.invokable;

import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.invoke.Invoker;

public class InvokerBuilderImpl<B> extends AbstractInvokerBuilder<B, Invoker<B, ?>> {

    public InvokerBuilderImpl(Class<B> beanClass, Method method, BeanManager beanManager) {
        super(beanClass, method, beanManager);
    }

    @Override
    public Invoker<B, ?> build() {
        return doBuild();
    }
}
