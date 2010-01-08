package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessManagedBean;

import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.manager.BeanManagerImpl;

public class ProcessManagedBeanImpl<X> extends AbstractProcessClassBean<X, ManagedBean<X>> implements ProcessManagedBean<X>
{

   public static <X> void fire(BeanManagerImpl beanManager, ManagedBean<X> bean)
   {
      new ProcessManagedBeanImpl<X>(beanManager, bean) {}.fire();
   }
   
   public ProcessManagedBeanImpl(BeanManagerImpl beanManager, ManagedBean<X> bean)
   {
      super(beanManager, ProcessManagedBean.class, new Type[] { bean.getAnnotatedItem().getBaseType() }, bean);
   }

   public AnnotatedType<X> getAnnotatedBeanClass()
   {
      return getBean().getAnnotatedItem();
   }

}
