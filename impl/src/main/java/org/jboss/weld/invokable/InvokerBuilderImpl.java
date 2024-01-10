package org.jboss.weld.invokable;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.manager.BeanManagerImpl;

public class InvokerBuilderImpl<B> extends AbstractInvokerBuilder<B, Invoker<B, ?>> {

    public InvokerBuilderImpl(AnnotatedType<B> beanClass, TargetMethod method, BeanManagerImpl beanManager) {
        super(beanClass, method, beanManager);
    }

    @Override
    public Invoker<B, ?> build() {
        return doBuild();
    }
}
