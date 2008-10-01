package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.manager.Manager;
import javax.webbeans.manager.Observer;
import javax.webbeans.Observes;

import org.jboss.webbeans.injectable.Parameter;
import org.jboss.webbeans.model.AbstractComponentModel;

/**
 * <p>
 * Reference implementation for the Observer interface, which represents an
 * observer method. Each observer method has an event type, the class of the
 * event object being observed, and event binding types, annotations applied to
 * the event parameter to narrow the event notifications delivered.
 * </p>
 * 
 * <p>
 * The hash code for each observer method includes all the information
 * contained. The data structure used to lookup observers must account for an
 * arbitrary number of observers of the same event.
 * </p>
 * 
 * @author David Allen
 * 
 */
public class ObserverImpl<T> implements Observer<T>
{

   private final AbstractComponentModel<?, ?> componentModel;
   private final ObserverMethod observerMethod;
   private final Set<Annotation> eventBindings;
   private final Class<T> eventType;

   /**
    * Creates an Observer which describes and encapsulates an observer method (7.3).
    * 
    * @param componentModel The model for the component which defines the
    *           observer method
    * @param observer The observer method to notify
    * @param eventType The type of event being observed
    */
   @SuppressWarnings("unchecked")
   public ObserverImpl(AbstractComponentModel<?, ?> componentModel, ObserverMethod observer, Class<T> eventType)
   {
      this.componentModel = componentModel;
      this.observerMethod = observer;
      this.eventType = eventType;
      List<Parameter> parms = observer.getParameters();
      eventBindings = new HashSet<Annotation>();
      for (Parameter p : parms)
      {
         if (p.getType().equals(eventType))
         {
            if ((p.getBindingTypes() != null) && (p.getBindingTypes().length > 0))
            {
               eventBindings.addAll(Arrays.asList(p.getBindingTypes()));
               // Remove the @Observes annotation since it is not an event
               // binding type
               for (Annotation annotation : eventBindings)
               {
                  if (Observes.class.isAssignableFrom(annotation.getClass()))
                     eventBindings.remove(annotation);
               }
               break;
            }
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.webbeans.Observer#getEventBindingTypes()
    */
   public Set<Annotation> getEventBindingTypes()
   {
      return Collections.unmodifiableSet(this.eventBindings);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.webbeans.Observer#getEventType()
    */
   public Class<T> getEventType()
   {
      return eventType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.webbeans.Observer#notify(javax.webbeans.Container,
    * java.lang.Object)
    */
   public void notify(Manager manager, T event)
   {
      // Get the most specialized instance of the component
      Object instance = getInstance(manager);
      if (instance != null)
         this.observerMethod.invoke(manager, instance, event);
   }

   /**
    * Uses the container to retrieve the most specialized instance of this
    * observer.
    * 
    * @param container The WebBeans manager
    * @return the most specialized instance
    */
   protected Object getInstance(Manager manager)
   {
      // Return the most specialized instance of the component
      return manager.getInstanceByType(componentModel.getType(), componentModel.getBindingTypes().toArray(new Annotation[0]));
   }
}
