package org.jboss.weld.invokable;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;

public class InvokerInfoBuilder<B> extends AbstractInvokerBuilder<B, InvokerInfo> {

    public InvokerInfoBuilder(AnnotatedType<B> beanClass, TargetMethod method, BeanManager beanManager) {
        super(beanClass, method, (BeanManagerImpl) ((WeldManager) beanManager).unwrap());
    }

    @Override
    public InvokerInfo build() {
        return doBuild();
    }
}
