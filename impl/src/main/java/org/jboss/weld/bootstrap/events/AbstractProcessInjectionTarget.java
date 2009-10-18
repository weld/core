package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.AbstractClassBean;

public abstract class AbstractProcessInjectionTarget<T> extends AbstractDefinitionContainerEvent
{

   public static <X> void fire(BeanManagerImpl beanManager, AbstractClassBean<X> bean)
   {
      new ProcessBeanInjectionTarget<X>(beanManager, bean) {}.fire();
   }
   
   public static <X> InjectionTarget<X> fire(BeanManagerImpl beanManager, AnnotatedType<X> annotatedType, InjectionTarget<X> injectionTarget)
   {
      ProcessSimpleInjectionTarget<X> processSimpleInjectionTarget = new ProcessSimpleInjectionTarget<X>(beanManager, annotatedType, injectionTarget) {};
      processSimpleInjectionTarget.fire();
      return processSimpleInjectionTarget.getInjectionTarget();
   }

   protected final AnnotatedType<T> annotatedType;

   public AbstractProcessInjectionTarget(BeanManagerImpl beanManager, AnnotatedType<T> annotatedType)
   {
      super(beanManager, ProcessInjectionTarget.class, new Type[] {annotatedType.getBaseType() });
      this.annotatedType = annotatedType;
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
   }

   public List<Throwable> getDefinitionErrors()
   {
      return super.getErrors();
   }

   public AnnotatedType<T> getAnnotatedType()
   {
      return annotatedType;
   }

}