package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;

import javax.webbeans.manager.Manager;
import javax.webbeans.manager.Observer;

import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.InjectableParameterWrapper;
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
   private final InjectableMethod<?> observerMethod;
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
   public ObserverImpl(AbstractComponentModel<?, ?> componentModel, InjectableMethod<?> observer, Class<T> eventType)
   {
      this.componentModel = componentModel;
      this.observerMethod = observer;
      this.eventType = eventType;
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
   @SuppressWarnings("unchecked")
   public void notify(Manager manager, final T event)
   {
      // Get the most specialized instance of the component
      Object instance = getInstance(manager);
      if (instance != null)
      {
         // Let the super class get the parameter values, but substitute the event
         // object so that we know for certain it is the correct one.
         for (int i = 0; i < observerMethod.getParameters().size(); i++)
         {
            InjectableParameter<?> parameter = observerMethod.getParameters().get(i);
            if (parameter.getType().isAssignableFrom(event.getClass()))
            {
               InjectableParameter<?> newParameter = new InjectableParameterWrapper(parameter)
               {
                  @Override
                  public Object getValue(Manager manager)
                  {
                     return event;
                  }
               };
               observerMethod.getParameters().set(i, newParameter);
            }
         }
         this.observerMethod.invoke(manager, instance);
      }
         
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
