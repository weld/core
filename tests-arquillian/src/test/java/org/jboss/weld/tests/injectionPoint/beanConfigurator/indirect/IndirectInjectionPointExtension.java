package org.jboss.weld.tests.injectionPoint.beanConfigurator.indirect;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;

public class IndirectInjectionPointExtension implements Extension {

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        event.addBean()
                .beanClass(InjectionPointResult.class)
                .scope(Dependent.class)
                .addType(InjectionPointResult.class)
                .produceWith(instance -> {
                    InjectionPointCaptor captor = instance.select(InjectionPointCaptor.class).get();
                    InjectionPoint ip = captor.injectionPoint;
                    return new InjectionPointResult(ip);
                });
    }
}
