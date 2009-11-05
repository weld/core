package org.jboss.weld.tests.unit.deployment.structure.extensions;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;

public class CountingObserver1 implements Extension
{
   
   private int beforeBeanDiscovery;
   private int processFooManagedBean;
   private int processBarManagedBean;

   public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event)
   {
      beforeBeanDiscovery++;
   }
   
   public void observerProcessFooManagedBean(@Observes ProcessManagedBean<Foo> event)
   {
      processFooManagedBean++;
   }
   
   public void observerProcessBarManagedBean(@Observes ProcessManagedBean<Bar> event)
   {
      processBarManagedBean++;
   }
   
   public int getBeforeBeanDiscovery()
   {
      return beforeBeanDiscovery;
   }
   
   public int getProcessFooManagedBean()
   {
      return processFooManagedBean;
   }
   
   public int getProcessBarManagedBean()
   {
      return processBarManagedBean;
   }
     
}
