package org.jboss.weld.test.unit.deployment.structure.extensions;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessProducer;

public class Observer1 extends ObserverBase implements Extension
{
   
   public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery event)
   {
      this.afterBeanDiscoveryCalled = true;
   }
   
   public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event)
   {
      this.beforeBeanDiscoveryCalled = true;
   }
   
   public void observeAfterDeploymentValidation(@Observes AfterDeploymentValidation event)
   {
      afterDeploymentValidationCalled = true;
   }
   
   public void observeProcessProducer(@Observes ProcessProducer<Foo, String> event)
   {
      processProducerCalled = true;
   }
   
   public void observeProcessInjectionTarget(@Observes ProcessInjectionTarget<Foo> event)
   {
      processInjectionTargetCalled = true;
   }
   
   public void observeProcessManagedBean(@Observes ProcessManagedBean<Foo> event)
   {
      processManagedBeanCalled = true;
   }

}
