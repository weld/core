package org.jboss.webbeans.test.mock;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.Observer;
import javax.webbeans.TypeLiteral;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;

public class MockManagerImpl extends ManagerImpl
{
   private Object       event = null;
   private Class<? extends Object>     eventType = null;
   private Annotation[] eventBindings = null;
   private Observer<?>  observer = null;

   /* (non-Javadoc)
    * @see org.jboss.webbeans.ManagerImpl#fireEvent(java.lang.Object, java.lang.annotation.Annotation[])
    */
   @Override
   public void fireEvent(Object event, Annotation... bindings)
   {
      // Record the event
      this.event = event;
      this.eventBindings = bindings;
   }

   /* (non-Javadoc)
    * @see org.jboss.webbeans.ManagerImpl#addObserver(javax.webbeans.Observer, java.lang.Class, java.lang.annotation.Annotation[])
    */
   @Override
   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType,
         Annotation... bindings)
   {
      this.observer = observer;
      this.eventType = eventType;
      this.eventBindings = bindings;
      return this;
   }

   /* (non-Javadoc)
    * @see org.jboss.webbeans.ManagerImpl#addObserver(javax.webbeans.Observer, javax.webbeans.TypeLiteral, java.lang.annotation.Annotation[])
    */
   @Override
   public <T> Manager addObserver(Observer<T> observer,
         TypeLiteral<T> eventType, Annotation... bindings)
   {
      this.observer = observer;
      // TODO Fix the event type based on the type literal being passed.  Not clear how to get the actual T.
      this.eventType = null;
      this.eventBindings = bindings;
      return this;
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
   public final Class<?> getEventType()
   {
      return eventType;
   }

   /**
    * @return the observer
    */
   public final Observer<?> getObserver()
   {
      return observer;
   }
   
   public void setEnabledDeploymentTypes(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      initEnabledDeploymentTypes(enabledDeploymentTypes);
   }

}
