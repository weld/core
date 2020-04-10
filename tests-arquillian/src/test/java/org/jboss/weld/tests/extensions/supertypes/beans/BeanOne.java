package org.jboss.weld.tests.extensions.supertypes.beans;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

/**
 *
 */
public class BeanOne {

    @Inject
    private BeanManager beanManager;

    public BeanManager getBeanManager() {
        return beanManager;
    }

}
