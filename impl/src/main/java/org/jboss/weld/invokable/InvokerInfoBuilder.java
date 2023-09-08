package org.jboss.weld.invokable;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.spi.BeanManager;

import java.lang.reflect.Method;

public class InvokerInfoBuilder<B> extends AbstractInvokerBuilder<B, InvokerInfo> {

    public InvokerInfoBuilder(Class<B> beanClass, Method method, BeanManager beanManager) {
        super(beanClass, method, beanManager);
    }

    @Override
    public InvokerInfo build() {
        return doBuild();
    }
}
