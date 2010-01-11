package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.ProcessProducerMethod;

import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.manager.BeanManagerImpl;

public class ProcessProducerMethodImpl<X, T> extends AbstractProcessProducerBean<X, T, ProducerMethod<X, T>> implements ProcessProducerMethod<X, T>
{
   
   
   public static <X, T> void fire(BeanManagerImpl beanManager, ProducerMethod<X, T> bean)
   {
      new ProcessProducerMethodImpl<X, T>(beanManager, bean) {}.fire();
   }

   public ProcessProducerMethodImpl(BeanManagerImpl beanManager, ProducerMethod<X, T> bean)
   {
      super(beanManager, ProcessProducerMethod.class, new Type[] { bean.getWeldAnnotated().getDeclaringType().getBaseType(), bean.getWeldAnnotated().getBaseType() }, bean);
   }

   public AnnotatedParameter<X> getAnnotatedDisposedParameter()
   {
      return getBean().getDisposalMethod().getDisposesParameter();
   }

   public AnnotatedMethod<X> getAnnotatedProducerMethod()
   {
      return getBean().getWeldAnnotated();
   }

   

}
