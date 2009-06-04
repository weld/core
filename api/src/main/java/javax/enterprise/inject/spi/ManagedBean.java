package javax.enterprise.inject.spi;

import java.util.Set;

public interface ManagedBean<X> extends Bean<X> {
    public AnnotatedType<X> getAnnotatedType();
    public InjectionTarget<X> getInjectionTarget();
    public Set<ProducerBean<X, ?>> getProducerBeans();
    public Set<ObserverMethod<X,?>> getObserverMethods();
    public Bean<X> getBeanClass();
}
