package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;

import javax.webbeans.Observer;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.InjectableParameterWrapper;
import org.jboss.webbeans.model.bean.BeanModel;

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

   private final BeanModel<?, ?> beanModel;
   private final InjectableMethod<? extends Object> observerMethod;
   private final Class<T> eventType;

   /**
    * Creates an Observer which describes and encapsulates an observer method (7.3).
    * 
    * @param beanModel The model for the bean which defines the
    *           observer method
    * @param observer The observer method to notify
    * @param eventType The type of event being observed
    */
   public ObserverImpl(BeanModel<?, ?> beanModel, InjectableMethod<?> observer, Class<T> eventType)
   {
      this.beanModel = beanModel;
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
   public void notify(final T event)
   {
      // Get the most specialized instance of the bean
      Object instance = null /*getInstance(manager)*/;
      if (instance != null)
      {
         // Let the super class get the parameter values, but substitute the event
         // object so that we know for certain it is the correct one.
         for (int i = 0; i < observerMethod.getParameters().size(); i++)
         {
            InjectableParameter<? extends Object> parameter = observerMethod.getParameters().get(i);
            if (parameter.getType().isAssignableFrom(event.getClass()))
            {
               InjectableParameter<?> newParameter = new InjectableParameterWrapper<Object>(parameter)
               {
                  @Override
                  public Object getValue(ManagerImpl manager)
                  {
                     return event;
                  }
               };
               observerMethod.getParameters().set(i, newParameter);
            }
         }
         // this.observerMethod.invoke(manager, instance);
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
      // Return the most specialized instance of the bean
      return manager.getInstanceByType(beanModel.getType(), beanModel.getBindingTypes().toArray(new Annotation[0]));
   }
}
