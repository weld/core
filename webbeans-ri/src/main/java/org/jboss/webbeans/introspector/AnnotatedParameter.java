package org.jboss.webbeans.introspector;

import org.jboss.webbeans.ManagerImpl;

public interface AnnotatedParameter<T> extends AnnotatedItem<T, Object>
{
   
   public T getValue(ManagerImpl manager);
   
}
