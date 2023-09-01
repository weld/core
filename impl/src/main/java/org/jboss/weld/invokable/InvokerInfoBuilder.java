package org.jboss.weld.invokable;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.spi.BeanManager;

import java.lang.reflect.Method;

public class InvokerInfoBuilder extends AbstractInvokerBuilder<InvokerInfo> {

    public InvokerInfoBuilder(Class<?> beanClass, Method method, BeanManager beanManager) {
        super(beanClass, method, beanManager);
    }

    @Override
    public InvokerInfo build() {
        return new InvokerImpl<>(this);
    }
}
