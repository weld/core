package org.jboss.weld.context.ejb;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.ejb.InvocationContextBeanStore;
import org.jboss.weld.context.cache.RequestScopedCache;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;

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
