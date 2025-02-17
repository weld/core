package org.jboss.weld.tests.el.injection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.el.ELAwareBeanManager;
import jakarta.inject.Inject;

@ApplicationScoped
public class ELAwareInjectionBean {

    @Inject
    private ELAwareBeanManager elAwareBeanManager;

    public ELAwareBeanManager getElAwareBeanManager() {
        return elAwareBeanManager;
    }
}
