package org.jboss.webbeans.injection;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.introspector.AnnotatedItem;

public interface AnnotatedInjectionPoint<T, S> extends InjectionPoint, AnnotatedItem<T, S>
{
   
   /**
    * Injects an instance
    * 
    * 
    * @param declaringInstance The instance to inject into
    * @param value The value to inject
    */
   public void inject(Object declaringInstance, Object value);
   
}
