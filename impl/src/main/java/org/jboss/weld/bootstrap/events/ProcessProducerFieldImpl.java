package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.ProcessProducerField;

import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.manager.BeanManagerImpl;

public class ProcessProducerFieldImpl<X, T> extends AbstractProcessProducerBean<X, T, ProducerField<X, T>> implements ProcessProducerField<X, T>
{

   
   public static <X, T> void fire(BeanManagerImpl beanManager, ProducerField<X, T> bean)
   {
      new ProcessProducerFieldImpl<X, T>(beanManager, bean) {}.fire();
   }
   
   public ProcessProducerFieldImpl(BeanManagerImpl beanManager, ProducerField<X, T> bean)
   {
      super(beanManager, ProcessProducerField.class, new Type[] { bean.getAnnotatedItem().getDeclaringType().getBaseType(), bean.getAnnotatedItem().getBaseType() }, bean);
   }

   public AnnotatedField<X> getAnnotatedProducerField()
   {
      return getBean().getAnnotatedItem();
   }

}
