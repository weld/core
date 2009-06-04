package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.event.Observer;

public interface ObserverMethod<X, T> extends Observer<T> {
    public AnnotatedMethod<? super X> getAnnotatedMethod();
    public Bean<X> getBean();
    public Type getObservedEventType();
    public Set<Annotation> getObservedEventBindings();
    public void notify(X instance, T event);
    public Set<InjectionPoint> getInjectionPoints();
}
