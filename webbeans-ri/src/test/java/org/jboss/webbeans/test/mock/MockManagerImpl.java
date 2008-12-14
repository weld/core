package org.jboss.webbeans.test.mock;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Observer;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;

public class MockManagerImpl extends ManagerImpl
{
   
   public static int BUILT_IN_BEANS = 4;
   
   private Object event = null;
   private Annotation[] eventBindings = null;
   private Class<? extends Object> observedEventType = null;

   @Override
   public void fireEvent(Object event, Annotation... bindings)
   {
      // Record the event
      this.event = event;
      this.eventBindings = bindings;
      super.fireEvent(event, bindings);
   }
   
   @Override
   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
	   this.observedEventType = eventType;
	   return super.addObserver(observer, eventType, bindings);
   }

   /**
    * Retrieves the event which was last fired with this manager.
    * @return the event
    */
   public final Object getEvent()
   {
      return event;
   }

   /**
    * @return the eventBindings
    */
   public final Set<Annotation> getEventBindings()
   {
      if (eventBindings != null)
         return new HashSet<Annotation>(Arrays.asList(eventBindings));
      else
         return null;
   }

   /**
    * @return the eventType
    */
   public final Class<?> getObservedEventType()
   {
      return observedEventType;
   }
   
}
