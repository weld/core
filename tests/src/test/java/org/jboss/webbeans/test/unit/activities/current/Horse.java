package org.jboss.webbeans.test.unit.activities.current;

import javax.enterprise.inject.Current;
import javax.enterprise.inject.spi.BeanManager;

class Horse
{
   
   @Current BeanManager beanManager;
   
   public BeanManager getManager()
   {
      return beanManager;
   }
   
}
