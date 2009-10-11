package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.ProcessBean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.AbstractProducerBean;

public abstract class AbstractProcessProducerBean<X, T, B extends AbstractProducerBean<X, T, ? >> extends AbstractDefinitionContainerEvent implements ProcessBean<T>
{

   private final B bean;
   
   public AbstractProcessProducerBean(BeanManagerImpl beanManager, Type rawType, Type[] actualTypeArguments, B bean)
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
