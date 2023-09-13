package org.jboss.weld.injection;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

/**
 * @author Stuart Douglas
 */
public abstract class ForwardingInjectionTarget<T> implements InjectionTarget<T> {

    protected abstract InjectionTarget<T> delegate();

    public void inject(final T instance, final CreationalContext<T> ctx) {
        delegate().inject(instance, ctx);
    }

    public void postConstruct(final T instance) {
        delegate().postConstruct(instance);
    }

    public void preDestroy(final T instance) {
        delegate().preDestroy(instance);
    }

    public T produce(final CreationalContext<T> ctx) {
        return delegate().produce(ctx);
    }

    public void dispose(final T instance) {
        delegate().dispose(instance);
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return delegate().getInjectionPoints();
    }
}
