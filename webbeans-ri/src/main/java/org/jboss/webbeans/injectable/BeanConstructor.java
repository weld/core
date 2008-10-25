package org.jboss.webbeans.injectable;

import org.jboss.webbeans.ManagerImpl;

public interface BeanConstructor<T>
{

   public T invoke(ManagerImpl manager);
   
}
