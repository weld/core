package org.jboss.weld.context.api;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public interface ContextualInstance<T>
{
   
   public T getInstance();
   
   public CreationalContext<T> getCreationalContext();
   
   public Contextual<T> getContextual();
}
