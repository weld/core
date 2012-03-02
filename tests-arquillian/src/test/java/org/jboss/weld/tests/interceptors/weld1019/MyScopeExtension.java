package org.jboss.weld.tests.interceptors.weld1019;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 *
 */
public class MyScopeExtension implements Extension {

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        event.addScope(MyScope.class, true, true);
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
        event.addContext(new MyScopeContext());
    }
}