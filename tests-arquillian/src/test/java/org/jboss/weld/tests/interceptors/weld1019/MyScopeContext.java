package org.jboss.weld.tests.interceptors.weld1019;

import java.lang.annotation.Annotation;

import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.contexts.AbstractContext;
import org.jboss.weld.contexts.beanstore.BeanStore;
import org.jboss.weld.contexts.beanstore.HashMapBeanStore;

/**
 *
 */
public class MyScopeContext extends AbstractContext {

    private HashMapBeanStore beanStore = new HashMapBeanStore();

    public MyScopeContext() {
        super(RegistrySingletonProvider.STATIC_INSTANCE, false);
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