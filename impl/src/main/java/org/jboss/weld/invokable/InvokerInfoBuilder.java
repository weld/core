package org.jboss.weld.invokable;

import java.lang.reflect.Method;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.spi.BeanManager;

public class InvokerInfoBuilder<B> extends AbstractInvokerBuilder<B, InvokerInfo> {

    public InvokerInfoBuilder(Class<B> beanClass, Method method, BeanManager beanManager) {
        super(beanClass, method, beanManager);
    }

    @Override
    public InvokerInfo build() {
        return doBuild();
    }
}
