package org.jboss.weld.bootstrap.events;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.jboss.weld.manager.BeanManagerImpl;


public class ProcessSimpleInjectionTarget<T> extends AbstractProcessInjectionTarget<T> implements ProcessInjectionTarget<T>
{
   
   private InjectionTarget<T> injectionTarget;

   public ProcessSimpleInjectionTarget(BeanManagerImpl beanManager, AnnotatedType<T> annotatedType, InjectionTarget<T> injectionTarget)
   {
      super(beanManager, annotatedType);
      this.injectionTarget = injectionTarget;
   }

   public InjectionTarget<T> getInjectionTarget()
   {
      return injectionTarget;
   }

   public void setInjectionTarget(InjectionTarget<T> injectionTarget)
   {
      this.injectionTarget = injectionTarget;
   }

}
