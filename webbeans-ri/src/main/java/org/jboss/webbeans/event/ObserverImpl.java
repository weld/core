package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;

import javax.webbeans.Current;
import javax.webbeans.Observer;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.InjectableParameterWrapper;
import org.jboss.webbeans.model.bean.BeanModel;

/**
 * <p>
 * Reference implementation for the Observer interface, which represents an
 * observer method. Each observer method has an event type which is the class of the
 * event object being observed, and event binding types that are annotations applied to
 * the event parameter to narrow the event notifications delivered.
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
    * Injected before notify is called. This can only work with the RI since
    * InjectableMethod requires this specific implementation. 
    * TODO Determine if the impl can be injected here.
    */
   @Current
   protected ManagerImpl manager;

   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (7.5).
    * 
    * @param componentModel
    *           The model for the component which defines the observer method
    * @param observer
    *           The observer method to notify
    * @param eventType
    *           The type of event being observed
    * @param beanModel The model for the bean which defines the
    *           observer method
    * @param observer The observer method to notify
    * @param eventType The type of event being observed
    */
   public ObserverImpl(final BeanModel<?, ?> beanModel,
         final InjectableMethod<?> observer, final Class<T> eventType)
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
      // Get the most specialized instance of the component
      Object instance = getInstance();
      if (instance != null)
      {
         // Let the super class get the parameter values, but substitute the event
         // object so that we know for certain it is the correct one.
         for (int i = 0; i < observerMethod.getParameters().size(); i++)
         {
            InjectableParameter<? extends Object> parameter = observerMethod
                  .getParameters().get(i);
            if (parameter.getType().isAssignableFrom(event.getClass()))
            {
               InjectableParameter<?> newParameter = new InjectableParameterWrapper<Object>(
                     parameter)
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
         this.observerMethod.invoke(manager, instance);
      }

   }

   /**
    * Uses the container to retrieve the most specialized instance of this
    * observer.
    * 
    * @return the most specialized instance
    */
   protected Object getInstance()
   {
      // Return the most specialized instance of the component
      return manager.getInstanceByType(beanModel.getType(), beanModel
            .getBindingTypes().toArray(new Annotation[0]));
   }
}
