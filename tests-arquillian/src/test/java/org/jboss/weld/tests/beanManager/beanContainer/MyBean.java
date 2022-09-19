package org.jboss.weld.tests.beanManager.beanContainer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Inject;

@ApplicationScoped
public class MyBean {

    @Inject
    BeanContainer beanContainer;

    public BeanContainer getBeanContainer() {
        return beanContainer;
    }
}
