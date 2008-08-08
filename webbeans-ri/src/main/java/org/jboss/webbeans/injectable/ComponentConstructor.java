package org.jboss.webbeans.injectable;

import javax.webbeans.Container;

public interface ComponentConstructor<T>
{

   public T invoke(Container container);
   
}
