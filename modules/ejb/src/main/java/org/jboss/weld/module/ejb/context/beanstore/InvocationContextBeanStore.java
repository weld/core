package org.jboss.weld.module.ejb.context.beanstore;

import java.util.Iterator;

import javax.interceptor.InvocationContext;

import org.jboss.weld.contexts.beanstore.AttributeBeanStore;
import org.jboss.weld.contexts.beanstore.LockStore;
import org.jboss.weld.contexts.beanstore.NamingScheme;

public class InvocationContextBeanStore extends AttributeBeanStore {

    private final InvocationContext ctx;

    public InvocationContextBeanStore(NamingScheme namingScheme, InvocationContext ctx) {
        super(namingScheme, false);
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
    protected Iterator<String> getAttributeNames() {
        return ctx.getContextData().keySet().iterator();
    }

    @Override
    protected void setAttribute(String prefixedId, Object instance) {
        ctx.getContextData().put(prefixedId, instance);
    }

    public LockStore getLockStore() {
        return null;
    }
}
