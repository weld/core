package org.jboss.weld.contexts.unbound;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.RequestScoped;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.contexts.AbstractUnboundContext;
import org.jboss.weld.contexts.beanstore.HashMapBeanStore;
import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.serialization.spi.BeanIdentifier;

public class RequestContextImpl extends AbstractUnboundContext implements RequestContext {

    public RequestContextImpl(String contextId) {
        super(contextId, false);
    }

    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    public void activate() {
        // Attach bean store (this context is unbound, so this can simply be thread-scoped
        setBeanStore(new HashMapBeanStore());
        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        // Detach the bean store
        setBeanStore(null);
        cleanup();
    }

    @Override
    public Collection<ContextualInstance<?>> getAllContextualInstances() {
        Set<ContextualInstance<?>> result = new HashSet<>();
        getBeanStore().iterator().forEachRemaining((BeanIdentifier beanId) -> {
            result.add(getBeanStore().get(beanId));
        });
        return result;
    }

    @Override
    public void clearAndSet(Collection<ContextualInstance<?>> setOfInstances) {
        getBeanStore().clear();
        // invalidate caches for req., session, conv. scopes
        // this might be needed for propagation on the thread where there are existing contexts
        RequestScopedCache.invalidate();
        for (ContextualInstance<?> contextualInstance : setOfInstances) {
            getBeanStore().put(getId(contextualInstance.getContextual()), contextualInstance);
        }
    }
}
