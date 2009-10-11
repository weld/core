package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.AbstractClassBean;


public class ProcessInjectionTargetImpl<T> extends AbstractDefinitionContainerEvent implements ProcessInjectionTarget<T>
{
   
   public static <X> void fire(BeanManagerImpl beanManager, AbstractClassBean<X> bean)
   {
      new ProcessInjectionTargetImpl<X>(beanManager, bean.getAnnotatedItem(), bean) {}.fire();
   }
   
   private final AnnotatedType<T> annotatedType;
   private AbstractClassBean<T> classBean;

   public ProcessInjectionTargetImpl(BeanManagerImpl beanManager, AnnotatedType<T> annotatedType, AbstractClassBean<T> bean)
   {
      super(beanManager, ProcessInjectionTarget.class, new Type[] { bean.getAnnotatedItem().getBaseType() });
      this.classBean = bean;
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

   public InjectionTarget<T> getInjectionTarget()
   {
      return classBean.getInjectionTarget();
   }

   public void setInjectionTarget(InjectionTarget<T> injectionTarget)
   {
      classBean.setInjectionTarget(injectionTarget);
   }

}
