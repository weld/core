/**
 * 
 */
package org.jboss.weld.manager;

class NamespaceTransform implements Transform<String>
{
   
   public Iterable<String> transform(BeanManagerImpl beanManager)
   {
      return beanManager.getNamespaces();
   }
   
}
