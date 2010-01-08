/**
 * 
 */
package org.jboss.weld.manager;

import javax.enterprise.inject.spi.ObserverMethod;

class ObserverMethodTransform implements Transform<ObserverMethod<?>>
{
   
   public Iterable<ObserverMethod<?>> transform(BeanManagerImpl beanManager)
   {
      return beanManager.getObservers();
   }
   
}
