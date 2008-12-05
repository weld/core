package org.jboss.webbeans.contexts;

import javax.webbeans.manager.Contextual;

import org.jboss.webbeans.ManagerImpl;

public abstract class AbstractBeanMapAdaptor implements BeanMap
{
   
   protected abstract String getKeyPrefix();
   
   /**
    * Returns a map key to a bean. Uses a known prefix and appends the index of
    * the Bean in the Manager bean list.
    * 
    * @param bean The bean to generate a key for.
    * @return A unique key;
    */
   protected String getBeanKey(Contextual<?> bean)
   {
      return getKeyPrefix() + "#" + ManagerImpl.instance().getBeans().indexOf(bean);
   }
   
}
