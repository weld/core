package org.jboss.weld.bootstrap.events;

import java.util.List;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.ProcessBean;

import org.jboss.weld.bean.AbstractBean;

public abstract class AbstractProcessBean<X, B extends AbstractBean<X, ?>> extends AbstractContainerEvent implements ProcessBean<X>
{

   private final B bean;
   
   public AbstractProcessBean(B bean)
   {
      this.bean = bean;
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
   }
   
   public List<Throwable> getDefinitionErrors()
   {
      return super.getErrors();
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
