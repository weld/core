package org.jboss.weld.tests.unit.deployment.structure.extensions;

import javax.enterprise.inject.spi.BeanManager;

public class ObserverBase
{

   protected boolean afterBeanDiscoveryCalled;
   protected boolean beforeBeanDiscoveryCalled;
   protected boolean afterDeploymentValidationCalled;
   protected boolean processProducerCalled;
   protected boolean processInjectionTargetCalled;
   protected boolean processManagedBeanCalled;
   
   protected BeanManager beforeBeanDiscoveryBeanManager;

   public ObserverBase()
   {
      super();
   }

   public boolean isAfterBeanDiscoveryCalled()
   {
      return afterBeanDiscoveryCalled;
   }

   public boolean isBeforeBeanDiscoveryCalled()
   {
      return beforeBeanDiscoveryCalled;
   }

   public boolean isAfterDeploymentValidationCalled()
   {
      return afterDeploymentValidationCalled;
   }

   public boolean isProcessProducerCalled()
   {
      return processProducerCalled;
   }

   public boolean isProcessInjectionTargetCalled()
   {
      return processInjectionTargetCalled;
   }

   public boolean isProcessManagedBeanCalled()
   {
      return processManagedBeanCalled;
   }
   
   public BeanManager getBeforeBeanDiscoveryBeanManager()
   {
      return beforeBeanDiscoveryBeanManager;
   }

}