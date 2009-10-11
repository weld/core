package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessBean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.resources.ClassTransformer;

public abstract class ProcessBeanImpl<X> extends AbstractDefinitionContainerEvent implements ProcessBean<X>
{

   public static <X> void fire(BeanManagerImpl beanManager, Bean<X> bean)
   {
      new ProcessBeanImpl<X>(beanManager, bean) {}.fire();
   }
   
   private final Bean<X> bean;
   private final WeldAnnotated<?, ?> annotated;
   
   public ProcessBeanImpl(BeanManagerImpl beanManager, Bean<X> bean)
   {
      super(beanManager, ProcessBean.class, new Type[] {bean.getBeanClass()});
      this.bean = bean;
      this.annotated = beanManager.getServices().get(ClassTransformer.class).loadClass(bean.getBeanClass());
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
   }

   public Annotated getAnnotated()
   {
      return annotated;
   }

   public Bean<X> getBean()
   {
      return bean;
   }

}
