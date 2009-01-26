package org.jboss.webbeans.injection;

import javax.inject.manager.InjectionPoint;
import javax.inject.manager.Manager;

import org.jboss.webbeans.introspector.AnnotatedItem;

public interface AnnotatedInjectionPoint<T, S> extends InjectionPoint, AnnotatedItem<T, S>
{
   
   /**
    * Injects using the value provided by the manager.
    * 
    * @param declaringInstance The instance to inject into
    * @param manager The Web Beans manager
    */
   public void inject(Object declaringInstance, Manager manager);
   
   /**
    * Injects an instance
    * 
    * 
    * @param declaringInstance The instance to inject into
    * @param value The value to inject
    */
   public void inject(Object declaringInstance, Object value);
   
}
