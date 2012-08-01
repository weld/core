package org.jboss.weld.context.beanstore.ejb;

import java.util.Collection;

import javax.interceptor.InvocationContext;

import org.jboss.weld.context.beanstore.AttributeBeanStore;
import org.jboss.weld.context.beanstore.LockStore;
import org.jboss.weld.context.beanstore.NamingScheme;

public class InvocationContextBeanStore extends AttributeBeanStore {

    private final InvocationContext ctx;

    public InvocationContextBeanStore(NamingScheme namingScheme, InvocationContext ctx) {
        super(namingScheme);
        this.ctx = ctx;
    }

    @Override
    protected Object getAttribute(String prefixedId) {
        return ctx.getContextData().get(prefixedId);
    }

    @Override
    protected void removeAttribute(String prefixedId) {
        ctx.getContextData().remove(prefixedId);
    }

    @Override
    protected Collection<String> getAttributeNames() {
        return ctx.getContextData().keySet();
    }

    @Override
    protected void setAttribute(String prefixedId, Object instance) {
        ctx.getContextData().put(prefixedId, instance);
    }

    public LockStore getLockStore() {
        return null;
    }
}
