package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.manager.BeanManagerImpl;


public class ProcessProducerImpl<X, T> extends AbstractDefinitionContainerEvent implements ProcessProducer<X, T>
{
   
   public static <X, T> void fire(BeanManagerImpl beanManager, AbstractProducerBean<X, T, Member> producer)
   {
      new ProcessProducerImpl<X, T>(beanManager, producer.getWeldAnnotated(), producer) {}.fire();
   }
   
   private final AnnotatedMember<X> annotatedMember;
   private AbstractProducerBean<X, T, ?> bean;

   public ProcessProducerImpl(BeanManagerImpl beanManager, AnnotatedMember<X> annotatedMember, AbstractProducerBean<X, T, ?> bean)
   {
      super(beanManager, ProcessProducer.class, new Type[] { bean.getWeldAnnotated().getDeclaringType().getBaseType(), bean.getWeldAnnotated().getBaseType() });
      this.bean = bean;
      this.annotatedMember = annotatedMember;
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
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
