package org.jboss.webbeans.injectable;

import org.jboss.webbeans.ManagerImpl;

public interface ComponentConstructor<T>
{

   public T invoke(ManagerImpl manager);
   
}
