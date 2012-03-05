package org.jboss.weld.tests.interceptors.weld1019;

import org.jboss.weld.context.AbstractContext;
import org.jboss.weld.context.beanstore.BeanStore;
import org.jboss.weld.context.beanstore.HashMapBeanStore;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;

/**
 *
 */
public class MyScopeContext extends AbstractContext {

    private HashMapBeanStore beanStore = new HashMapBeanStore();

    public MyScopeContext() {
        super(false);
    }

    public Class<? extends Annotation> getScope() {
        return MyScope.class;
    }

    @Override
    protected BeanStore getBeanStore() {
        return beanStore;
    }

    public boolean isActive() {
        return true;
    }
}