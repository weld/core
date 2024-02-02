package org.jboss.weld.environment.se.test.synthethic.extension;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

public class AfterBeanDiscoveryAddFooInjectedExtension implements Extension {
    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        AnnotatedType<FooInjected> annotatedType = beanManager.createAnnotatedType(FooInjected.class);
        BeanAttributes<FooInjected> beanAttributes = beanManager.createBeanAttributes(annotatedType);
        InjectionTargetFactory<FooInjected> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        Bean<FooInjected> bean = beanManager.createBean(beanAttributes, FooInjected.class, injectionTargetFactory);
        afterBeanDiscovery.addBean(bean);
    }
}
