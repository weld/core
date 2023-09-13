package org.jboss.weld.module.ejb.context;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.RequestScoped;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.context.ejb.EjbRequestContext;
import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SimpleNamingScheme;
import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.module.ejb.context.beanstore.InvocationContextBeanStore;

public class EjbRequestContextImpl extends AbstractBoundContext<InvocationContext> implements EjbRequestContext {

    private final NamingScheme namingScheme;

    public EjbRequestContextImpl(String contextId) {
        super(contextId, false);
        this.namingScheme = new SimpleNamingScheme(EjbRequestContext.class.getName());
    }

    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    public boolean associate(InvocationContext ctx) {
        if (getBeanStore() == null) {
            // Don't reassociate
            setBeanStore(new InvocationContextBeanStore(namingScheme, ctx));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void activate() {
        super.activate();
        RequestScopedCache.beginRequest();
    }

    @Override
    public void deactivate() {
        try {
            RequestScopedCache.endRequest();
        } finally {
            super.deactivate();
        }
    }
}
