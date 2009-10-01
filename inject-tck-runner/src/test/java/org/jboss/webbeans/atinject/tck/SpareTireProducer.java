package org.jboss.webbeans.atinject.tck;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.atinject.tck.auto.accessories.SpareTire;

public class SpareTireProducer extends AbstractProducer<SpareTire>
{

   @Inject
   public SpareTireProducer(BeanManager beanManager)
   {
      super(beanManager, SpareTire.class);
   }
   
   @Override
   @Produces @Named("spare")
   public SpareTire produce()
   {
      return super.produce();
   }
   
}
