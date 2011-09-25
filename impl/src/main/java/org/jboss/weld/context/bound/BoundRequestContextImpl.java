package org.jboss.weld.context.bound;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.cache.RequestScopedBeanCache;

import javax.enterprise.context.RequestScoped;
import java.lang.annotation.Annotation;
import java.util.Map;

public class BoundRequestContextImpl extends AbstractBoundContext<Map<String, Object>> implements BoundRequestContext {

    private static final String IDENTIFIER = BoundRequestContextImpl.class.getName();

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
            storage.put(IDENTIFIER, IDENTIFIER);
            setBeanStore(new MapBeanStore(namingScheme, storage));
            getBeanStore().attach();
            return true;
        } else {
            return false;
        }
    }

    public boolean dissociate(Map<String, Object> storage) {
        if (storage.containsKey(IDENTIFIER)) {
            try {
                storage.remove(IDENTIFIER);
                setBeanStore(null);

                return true;
            } finally {
                cleanup();
            }
        } else {
            return false;
        }
    }

    @Override
    public void activate() {
        super.activate();
        RequestScopedBeanCache.beginRequest();
    }

    @Override
    public void deactivate() {
        try {
            RequestScopedBeanCache.endRequest();
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
