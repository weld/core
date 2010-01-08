/**
 * 
 */
package org.jboss.weld.manager;

interface Transform<T>
{

   public Iterable<T> transform(BeanManagerImpl beanManager);
   
}
