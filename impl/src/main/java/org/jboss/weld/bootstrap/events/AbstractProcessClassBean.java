package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.ProcessBean;

import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.manager.BeanManagerImpl;

public abstract class AbstractProcessClassBean<X, B extends AbstractClassBean<X>> extends AbstractDefinitionContainerEvent implements ProcessBean<X>
{

   private final B bean;
   
   public AbstractProcessClassBean(BeanManagerImpl beanManager, Type rawType, Type[] actualTypeArguments, B bean)
   {
      super(beanManager, rawType, actualTypeArguments);
      this.bean = bean;
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
   }

   public Annotated getAnnotated()
   {
      return bean.getAnnotatedItem();
   }

   public B getBean()
   {
      return bean;
   }

}
