package org.jboss.weld.bootstrap.events;

import java.util.List;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.jboss.weld.bean.AbstractClassBean;


public class ProcessInjectionTargetImpl<T> extends AbstractContainerEvent implements ProcessInjectionTarget<T>
{
   
   private final AnnotatedType<T> annotatedType;
   private AbstractClassBean<T> classBean;

   public ProcessInjectionTargetImpl(AnnotatedType<T> annotatedType, AbstractClassBean<T> classBean)
   {
      this.classBean = classBean;
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
