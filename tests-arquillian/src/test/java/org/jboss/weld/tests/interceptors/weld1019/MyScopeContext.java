package org.jboss.weld.tests.interceptors.weld1019;

import java.lang.annotation.Annotation;

import org.jboss.weld.context.AbstractContext;
import org.jboss.weld.context.beanstore.BeanStore;
import org.jboss.weld.context.beanstore.HashMapBeanStore;

/**
 *
 */
public class MyScopeContext extends AbstractContext {

    private HashMapBeanStore beanStore = new HashMapBeanStore();

    public MyScopeContext() {
        super("STATIC_INSTANCE", false);
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