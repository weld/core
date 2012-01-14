package org.jboss.weld.bean.builtin;

import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public class ContextBean<T extends Context> extends AbstractBuiltInBean<T> {


    public static <T extends Context> ContextBean<T> of(ContextHolder<T> context, BeanManagerImpl beanManager) {
        return new ContextBean<T>(context, beanManager);
    }

    private final T context;
    private final Set<Type> types;
    private final Set<Annotation> qualifiers;

    public ContextBean(ContextHolder<T> contextHolder, BeanManagerImpl beanManager) {
        super(contextHolder.getType().getName(), beanManager, contextHolder.getType());
        this.context = contextHolder.getContext();
        this.types = new HierarchyDiscovery(contextHolder.getType()).getTypeClosure();
        this.qualifiers = contextHolder.getQualifiers();
    }

    public Set<Type> getTypes() {
        return types;
    }

    public T create(CreationalContext<T> creationalContext) {
        return context;
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        // No-op, this bean is just exposing stuff
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

}
