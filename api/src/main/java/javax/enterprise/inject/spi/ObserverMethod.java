package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.event.Notify;
import javax.enterprise.event.TransactionPhase;

public interface ObserverMethod<X, T>
{
   public Bean<X> getBean();

   public Type getObservedType();

   public Set<Annotation> getObservedBindings();

   public Notify getNotify();

   public TransactionPhase getTransactionPhase();

   public void notify(T event);
}
