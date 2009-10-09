package org.jboss.weld.bootstrap.events;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessManagedBean;

import org.jboss.weld.bean.ManagedBean;

public class ProcessManagedBeanImpl<X> extends AbstractProcessBean<X, ManagedBean<X>> implements ProcessManagedBean<X>
{

   public ProcessManagedBeanImpl(ManagedBean<X> bean)
   {
      super(bean);
   }

   public AnnotatedType<X> getAnnotatedBeanClass()
   {
      return getBean().getAnnotatedItem();
   }

}
