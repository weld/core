package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.Container;
import javax.webbeans.Observer;

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

   private final AbstractComponentModel<?, ?> compModel;
   private final ObserverMethod            observerMethod;
   private final Set<Annotation>           eventBindings;
   private final Class<T>                  eventType;

   /**
    * Creates an Observer which describes an observer method (7.3).
    * 
    * @param componentModel
    *           The model for the component which defines the observer method
    * @param observer
    *           The observer method to notify
    * @param eventType
    *           The type of event being observed
    */
   @SuppressWarnings("unchecked")
   public ObserverImpl(AbstractComponentModel<?, ?> componentModel,
         ObserverMethod observer, Class<T> eventType)
   {
      this.compModel = componentModel;
      this.observerMethod = observer;
      this.eventType = eventType;
      List<Parameter> parms = observer.getParameters();
      eventBindings = new HashSet<Annotation>();
      for (Parameter p : parms)
      {
         if (p.getType().equals(eventType))
         {
            if ((p.getBindingTypes() != null)
                  && (p.getBindingTypes().length > 0))
            {
               eventBindings.addAll(Arrays.asList(p.getBindingTypes()));
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
    *      java.lang.Object)
    */
   public void notify(Container container, T event)
   {
      // Get the most specialized instance of the component
      Object instance = container.getInstanceByType(compModel.getType(),
            compModel.getBindingTypes().toArray(new Annotation[0]));
      if (instance != null)
         this.observerMethod.invoke(container, instance, event);
   }
}
