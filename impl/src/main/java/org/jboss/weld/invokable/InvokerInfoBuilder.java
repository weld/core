package org.jboss.weld.invokable;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;

import java.lang.reflect.Method;

public class InvokerInfoBuilder extends AbstractInvokerBuilder<InvokerInfo> {

    public InvokerInfoBuilder(Class<?> beanClass, Method method) {
        super(beanClass, method);
    }

    @Override
    public InvokerInfo build() {
        return new InvokerImpl<>(this);
    }
}
