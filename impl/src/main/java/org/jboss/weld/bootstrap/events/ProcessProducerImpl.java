package org.jboss.weld.bootstrap.events;

import java.util.List;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.bean.AbstractProducerBean;


public class ProcessProducerImpl<X, T> extends AbstractContainerEvent implements ProcessProducer<X, T>
{
   
   private final AnnotatedMember<X> annotatedMember;
   private AbstractProducerBean<X, T, ?> bean;

   public ProcessProducerImpl(AnnotatedMember<X> annotatedMember, AbstractProducerBean<X, T, ?> bean)
   {
      this.bean = bean;
      this.annotatedMember = annotatedMember;
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
   }
   
   public List<Throwable> getDefinitionErrors()
   {
      return super.getErrors();
   }

   public AnnotatedMember<X> getAnnotatedMember()
   {
      return annotatedMember;
   }

   public Producer<T> getProducer()
   {
      return bean.getProducer();
   }

   public void setProducer(Producer<T> producer)
   {
      this.bean.setProducer(producer);
   }

}
