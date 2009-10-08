package org.jboss.weld.atinject.tck;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

public class AbstractProducer<T>
{

   protected final InjectionTarget<T> injectionTarget;
   protected final BeanManager beanManager;

   public AbstractProducer(BeanManager beanManager, Class<T> type)
   {
      this.injectionTarget = beanManager.createInjectionTarget(beanManager.createAnnotatedType(type));
      this.beanManager = beanManager;
   }

   public T produce()
   {
      CreationalContext<T> ctx = beanManager.createCreationalContext(null);
      T instance = injectionTarget.produce(ctx);
      injectionTarget.inject(instance, ctx);
      return instance;
   }

}