package org.jboss.weld.contexts.bound;

import java.lang.annotation.Annotation;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;

import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.MapBeanStore;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SimpleNamingScheme;
import org.jboss.weld.contexts.cache.RequestScopedCache;

public class BoundRequestContextImpl extends AbstractBoundContext<Map<String, Object>> implements BoundRequestContext {

    private final NamingScheme namingScheme;

    public BoundRequestContextImpl(String contextId) {
        super(contextId, false);
        this.namingScheme = new SimpleNamingScheme(BoundRequestContext.class.getName());
    }

    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    public boolean associate(Map<String, Object> storage) {
        if (getBeanStore() == null) {
            setBeanStore(new MapBeanStore(namingScheme, storage, true));
            getBeanStore().attach();
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

    @Override
    public void invalidate() {
        super.invalidate();
        getBeanStore().detach();
    }
}
