package javax.enterprise.inject.spi;

public interface ProducerBean<X, T> extends Bean<T> {
    public AnnotatedMember<? super X> getAnnotatedProducer();
    public AnnotatedMethod<? super X> getAnnotatedDisposer();
    public Bean<X> getBean();
    public InjectionTarget<T> getInjectionTarget();
    public Bean<X> getBeanClass();
}
