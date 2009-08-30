package org.jboss.webbeans.test.unit.activities.current;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

class Horse
{
   
   @Inject BeanManager beanManager;
   
   public BeanManager getManager()
   {
      return beanManager;
   }
   
}
