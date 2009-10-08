package org.jboss.weld.bootstrap.events;

import java.util.List;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;


public class ProcessProducerImpl<X, T> extends AbstractContainerEvent implements ProcessProducer<X, T>
{
   
   private final AnnotatedMember<X> annotatedMember;
   private Producer<T> producer;

   public ProcessProducerImpl(AnnotatedMember<X> annotatedMember, Producer<T> producer)
   {
      this.producer = producer;
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
      return producer;
   }

   public void setProducer(Producer<T> producer)
   {
      throw new UnsupportedOperationException();
   }

}
