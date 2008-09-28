package org.jboss.webbeans.injectable;

import javax.webbeans.manager.Manager;

public interface ComponentConstructor<T>
{

   public T invoke(Manager container);
   
}
