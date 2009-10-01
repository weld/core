package org.jboss.webbeans.atinject.tck;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;

public class DriversSeatProducer extends AbstractProducer<DriversSeat>
{
   
   @Inject
   public DriversSeatProducer(BeanManager beanManager)
   {
      super(beanManager, DriversSeat.class);
   }

   @Override
   @Produces @Drivers
   public DriversSeat produce()
   {
      return super.produce();
   }
   
}
