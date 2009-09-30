package org.jboss.webbeans.atinject.tck;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Seat;

public class DriversSeatProducer
{
   
   private final InjectionTarget<DriversSeat> injectionTarget;
   private final BeanManager beanManager;
   
   @Inject
   public DriversSeatProducer(BeanManager beanManager)
   {
      this.injectionTarget = beanManager.createInjectionTarget(beanManager.createAnnotatedType(DriversSeat.class));
      this.beanManager = beanManager;
   }

   @Produces @Drivers
   public Seat produceDriversSeat()
   {
      return injectionTarget.produce(beanManager.<DriversSeat>createCreationalContext(null));
   }
   
}
