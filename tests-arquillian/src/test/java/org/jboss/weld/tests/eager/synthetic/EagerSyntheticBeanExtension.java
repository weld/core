package org.jboss.weld.tests.eager.synthetic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

public class EagerSyntheticBeanExtension implements Extension {

    public void addEagerSyntheticBean(@Observes AfterBeanDiscovery abd) {
        abd.addBean()
                .beanClass(EagerSyntheticBean.class)
                .addType(EagerSyntheticBean.class)
                .scope(ApplicationScoped.class)
                .eager(true)
                .produceWith(instance -> {
                    EagerSyntheticBean bean = new EagerSyntheticBean();
                    EagerSyntheticBean.created = true;
                    return bean;
                });
    }
}
