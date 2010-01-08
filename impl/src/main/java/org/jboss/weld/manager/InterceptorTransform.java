/**
 * 
 */
package org.jboss.weld.manager;

import javax.enterprise.inject.spi.Interceptor;

class InterceptorTransform implements Transform<Interceptor<?>>
{
   
   public Iterable<Interceptor<?>> transform(BeanManagerImpl beanManager)
   {
      return beanManager.getInterceptors();
   }
   
}
