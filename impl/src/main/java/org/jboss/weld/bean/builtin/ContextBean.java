package org.jboss.weld.bean.builtin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

public class ContextBean<T extends Context> extends AbstractBuiltInBean<T> {

    public static <T extends Context> ContextBean<T> of(ContextHolder<T> context, BeanManagerImpl beanManager) {
        return new ContextBean<T>(context, beanManager);
    }

    private final T context;
    private final Set<Type> types;
    private final Set<Annotation> qualifiers;

    public ContextBean(ContextHolder<T> contextHolder, BeanManagerImpl beanManager) {
        super(new StringBeanIdentifier(BeanIdentifiers.forBuiltInBean(beanManager, contextHolder.getType(), null)), beanManager,
                contextHolder.getType());
        this.context = contextHolder.getContext();
        this.types = HierarchyDiscovery.forNormalizedType(contextHolder.getType()).getTypeClosure();
        this.qualifiers = contextHolder.getQualifiers();
    }

    public Set<Type> getTypes() {
        return types;
    }

    public T create(CreationalContext<T> creationalContext) {
        return context;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

}
