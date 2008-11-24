package org.jboss.webbeans.test.mock;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;

/**
 * An implementation used for unit testing only.
 * 
 * @author David Allen
 * 
 */
public class MockObserverImpl<T> extends ObserverImpl<T>
{

   private Object specializedInstance;

   public MockObserverImpl(ManagerImpl manager, EventBean<T> beanModel, AnnotatedMethod<Object> observer, Class<T> eventType)
   {
      super(manager, beanModel, observer, eventType);
   }

   @Override
   protected final Object getInstance(boolean conditional)
   {
      return specializedInstance;
   }

   /**
    * The most specialized instance of this observer type.
    * 
    * @param instance The instance to use for testing
    */
   public final void setInstance(Object instance)
   {
      specializedInstance = instance;
   }

}
