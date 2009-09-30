package org.jboss.webbeans.atinject.tck;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;
import javax.inject.Named;

import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.accessories.SpareTire;

public class SpareTireProducer
{
   
   private final InjectionTarget<SpareTire> injectionTarget;
   private final BeanManager beanManager;
   
   @Inject
   public SpareTireProducer(BeanManager beanManager)
   {
      this.injectionTarget = beanManager.createInjectionTarget(beanManager.createAnnotatedType(SpareTire.class));
      this.beanManager = beanManager;
   }

   @Produces @Named("spare")
   public Tire produceSpareTire()
   {
      return injectionTarget.produce(beanManager.<SpareTire>createCreationalContext(null));
   }
   
}
