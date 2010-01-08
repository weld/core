package org.jboss.weld.bootstrap.events;

import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.manager.BeanManagerImpl;


public class ProcessBeanInjectionTarget<T> extends AbstractProcessInjectionTarget<T> implements ProcessInjectionTarget<T>
{
   
   private final AbstractClassBean<T> classBean;

   public ProcessBeanInjectionTarget(BeanManagerImpl beanManager, AbstractClassBean<T> bean)
   {
      super(beanManager, bean.getAnnotatedItem());
      this.classBean = bean;
   }

   public InjectionTarget<T> getInjectionTarget()
   {
      return classBean.getInjectionTarget();
   }

   public void setInjectionTarget(InjectionTarget<T> injectionTarget)
   {
      classBean.setInjectionTarget(injectionTarget);
   }

}
