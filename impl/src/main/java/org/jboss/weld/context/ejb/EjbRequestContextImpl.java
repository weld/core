package org.jboss.weld.context.ejb;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.ejb.InvocationContextBeanStore;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;

public class EjbRequestContextImpl extends AbstractBoundContext<InvocationContext> implements EjbRequestContext {

    private static final String IDENTIFIER = EjbRequestContextImpl.class.getName();

    private final NamingScheme namingScheme;

    public EjbRequestContextImpl(String contextId) {
        super(contextId, false);
        this.namingScheme = new SimpleNamingScheme(EjbRequestContext.class.getName());
    }

    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    public boolean associate(InvocationContext ctx) {
        if (!ctx.getContextData().containsKey(IDENTIFIER)) {
            // Don't reassociate
            ctx.getContextData().put(IDENTIFIER, IDENTIFIER);
            setBeanStore(new InvocationContextBeanStore(namingScheme, ctx));

            return true;
        } else {
            return false;
        }
    }

    public boolean dissociate(InvocationContext ctx) {
        if (ctx.getContextData().containsKey(IDENTIFIER)) {
            try {
                setBeanStore(null);
                ctx.getContextData().remove(IDENTIFIER);
                return true;
            } finally {
                cleanup();
            }
        } else {
            return false;
        }

    }

}
