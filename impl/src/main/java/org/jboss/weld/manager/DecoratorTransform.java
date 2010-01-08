/**
 * 
 */
package org.jboss.weld.manager;

import javax.enterprise.inject.spi.Decorator;

class DecoratorTransform implements Transform<Decorator<?>>
{
   
   public Iterable<Decorator<?>> transform(BeanManagerImpl beanManager)
   {
      return beanManager.getDecorators();
   }
   
}
