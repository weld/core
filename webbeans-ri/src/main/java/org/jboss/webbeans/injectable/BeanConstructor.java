package org.jboss.webbeans.injectable;

import org.jboss.webbeans.ManagerImpl;

public interface BeanConstructor<T, S>
{

   public T invoke(ManagerImpl manager, Object instance);
   
}
