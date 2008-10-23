package org.jboss.webbeans.test.mock;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;

public class MockManagerImpl extends ManagerImpl
{
   private Object       event = null;
   private Annotation[] eventBindings = null;

   public MockManagerImpl(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      super(enabledDeploymentTypes);
   }

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

}
