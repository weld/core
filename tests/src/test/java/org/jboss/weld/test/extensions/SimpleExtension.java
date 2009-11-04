package org.jboss.weld.test.extensions;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;


public class SimpleExtension implements Extension
{
   
   private static SimpleExtension instance;
   
   public void observe(@Observes BeforeBeanDiscovery event)
   {
      SimpleExtension.instance = this;
   }
   
   public static SimpleExtension getInstance()
   {
      return instance;
   }

}
