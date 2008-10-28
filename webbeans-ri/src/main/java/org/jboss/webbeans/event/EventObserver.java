package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.webbeans.Observer;

/**
 * <p>
 * EventObserver wraps various {@link Observer} objects created by application
 * code or by the Web Beans Manager for annotated observer methods. In all
 * cases, this wrapper provides consistent object identification and hashing
 * based on the type of event being observed and any event binding types
 * specified.  It also provides a query method to quickly determine if a
 * set of event bindings are exactly what the observer is interested in receiving.
 * </p>
 * 
 * @author David Allen
 * 
 */
public class EventObserver<T>
{
   
   private final Class<T>         eventType;
   private final List<Annotation> eventBindings;
   private final Observer<T>      observer;

   /**
    * Constructs a new wrapper for an observer.
    * 
    * @param observer
    *           The observer
    * @param eventType
    *           The class of event being observed
    * @param eventBindings
    *           The array of annotation event bindings, if any
    */
   public EventObserver(final Observer<T> observer, final Class<T> eventType,
         final Annotation... eventBindings)
   {
      this.observer = observer;
      this.eventType = eventType;
      this.eventBindings = Arrays.asList(eventBindings);
   }

   /**
    * @return the eventType
    */
   public final Class<T> getEventType()
   {
      return eventType;
   }

   /**
    * @return the eventBindings
    */
   public final List<Annotation> getEventBindings()
   {
      return eventBindings;
   }

   /**
    * @return the observer
    */
   public final Observer<T> getObserver()
   {
      return observer;
   }

   /**
    * Query method to determine if this observer should be notified of an event
    * based on the event bindings and any member values thereof.
    * 
    * @param bindings The event bindings
    * @return true only if all required bindings match
    */
   public boolean isObserverInterested(Annotation... bindings)
   {
      // Simply check that all event bindings specified by the observer are
      // in the list provided.
      List<Annotation> bindingsArray = Arrays.asList(bindings);
      boolean result = true;
      if (!this.eventBindings.isEmpty())
         result = bindingsArray.containsAll(this.eventBindings);
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((eventBindings == null) ? 0 : eventBindings.hashCode());
      result = prime * result
            + ((eventType == null) ? 0 : eventType.hashCode());
      result = prime * result + ((observer == null) ? 0 : observer.hashCode());
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      EventObserver<?> other = (EventObserver<?>) obj;
      if (eventBindings == null)
      {
         if (other.eventBindings != null)
            return false;
      } else if (!eventBindings.equals(other.eventBindings))
         return false;
      if (eventType == null)
      {
         if (other.eventType != null)
            return false;
      } else if (!eventType.equals(other.eventType))
         return false;
      if (observer == null)
      {
         if (other.observer != null)
            return false;
      } else if (!observer.equals(other.observer))
         return false;
      return true;
   }

}
