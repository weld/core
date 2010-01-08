/**
 * 
 */
package org.jboss.weld.manager;

import javax.enterprise.inject.spi.Bean;

public class BeanTransform implements Transform<Bean<?>>
{
   
   private final BeanManagerImpl declaringBeanManager;

   public BeanTransform(BeanManagerImpl declaringBeanManager)
   {
      this.declaringBeanManager = declaringBeanManager;
   }

   public Iterable<Bean<?>> transform(BeanManagerImpl beanManager)
   {
      // New beans and built in beans aren't resolvable transitively
      if (beanManager.equals(declaringBeanManager))
      {
         return beanManager.getBeans();
      }
      else
      {
         return beanManager.getTransitiveBeans();
      }
   }
   
}