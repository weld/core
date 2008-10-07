package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import javax.webbeans.Current;
import javax.webbeans.Event;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.model.EventComponentModel;

/**
 * Implementation of an event as a simple component.
 * 
 * @author David Allen
 *
 */
public class EventImpl<T> extends BeanImpl<T> implements Event<T>
{
   // The current WB manager
   @Current
   protected Manager webBeansManager;

   /**
    * Creates a simple implementation of {@link Event} with no default
    * event bindings.
    */
   public EventImpl(EventComponentModel<T> componentMetaModel) {
      super(componentMetaModel);
   }
 
   /* (non-Javadoc)
    * @see javax.webbeans.Event#fire(java.lang.Object, java.lang.annotation.Annotation[])
    */
   public void fire(T event, Annotation... bindings)
   {
      // Combine the annotations passed here with the annotations (event bindings)
      // specified on the @Observable object.
      Set<Annotation> eventBindings = new HashSet<Annotation>();
      eventBindings.addAll(this.getBindingTypes());
      eventBindings.addAll(Arrays.asList(bindings));
      
      // Invoke the container method to fire the event per 7.2
      webBeansManager.fireEvent(event, eventBindings.toArray(new Annotation[0]));
   }

   // TODO Remove the setter for the manager once WB injection is working
   public void setManager(Manager manager)
   {
      this.webBeansManager = manager;
   }
}
