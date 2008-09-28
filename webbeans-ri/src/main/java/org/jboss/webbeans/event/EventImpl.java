package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import javax.webbeans.Event;

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
   private EventComponentModel<T> componentModel;

   /**
    * Creates a simple implementation of {@link Event} with no default
    * event bindings.
    */
   public EventImpl(EventComponentModel<T> componentMetaModel) {
      super(componentMetaModel);
      this.componentModel = componentMetaModel;
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
      componentModel.getContainer().fireEvent(event, eventBindings.toArray(new Annotation[0]));
   }

}
