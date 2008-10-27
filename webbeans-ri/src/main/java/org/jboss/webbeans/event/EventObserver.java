package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.webbeans.Observer;

/**
 * <p>
 * EventObserver wraps various {@link Observer} objects created by application
 * code or by the Web Beans Manager for annotated observer methods. In all
 * cases, this wrapper provides consistent object identification and hashing
 * based on the type of event being observed and any event binding types
 * specified.
 * 
 * @author David Allen
 * 
 */
public class EventObserver<T>
{
   
   // TODO This probably should be an injectable or annotated item
   
   private final Class<T> eventType;
   private final Annotation[] eventBindings;
   private final Observer<T> observer;

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
      this.eventBindings = eventBindings;
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
   public final Annotation[] getEventBindings()
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
      // TODO This logic needs to be in injectable
      boolean result = true;
      // Check each binding specified by this observer against those provided
      if (this.eventBindings != null)
      {
         if ((bindings != null) && (bindings.length > 0))
         {
            List<Annotation> bindingsArray = Arrays.asList(bindings);
            for (Annotation annotation : this.eventBindings)
            {
               int eventBindingIndex = bindingsArray.indexOf(annotation);
               if (eventBindingIndex >= 0)
               {
                  // TODO Use annotation equality
                  result = annotationsMatch(annotation, bindingsArray.get(eventBindingIndex));
               } else
               {
                  result = false;
                  break;
               }
            }
         }
      }
      return result;
   }

   /**
    * Compares two annotation bindings for equivalence.  This means that all member values, if any,
    * must match.
    * @param annotation The first annotation to compare
    * @param annotation2 The second annotation to compare
    * @return
    * 
    */
   private boolean annotationsMatch(Annotation annotation,
         Annotation annotation2)
   {
      boolean result = true;
      if (annotation.getClass().getDeclaredMethods().length > 0)
      {
         for (Method annotationValue : annotation.getClass().getDeclaredMethods())
         {
            try
            {
               if (!annotationValue.invoke(annotation).equals(annotationValue.invoke(annotation2)))
               {
                  result = false;
                  break;
               }
            } catch (Exception e)
            {
               result = false;
               break;
            }
         }
      }
      return result;
   }

   // TODO Implement equals and hashCode
   
}
