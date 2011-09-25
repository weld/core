package org.jboss.weld.tests.extensions.supertypes.beans;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

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
