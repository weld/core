package org.jboss.weld.context.unbound;

import org.jboss.weld.context.AbstractUnboundContext;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.beanstore.HashMapBeanStore;

import javax.enterprise.context.RequestScoped;
import java.lang.annotation.Annotation;

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
        // Dettach the bean store
        setBeanStore(null);
        cleanup();
    }

}
