package org.jboss.webbeans.event;

import javax.transaction.Synchronization;
import javax.webbeans.Observer;

/**
 * A synchronization object which will deliver the event to the observer
 * after the JTA transaction currently in effect is committed.
 * 
 * @author David Allen
 *
 */
public class DeferredEventNotification implements Synchronization
{
   private Observer<?> observer;
   private Object event;
   
   /**
    * Creates a new deferred event notifier.
    * 
    * @param manager The Web Beans manager
    * @param observer The observer to be notified
    * @param event The event being fired
    */
   public DeferredEventNotification(Object event, Observer<?> observer)
   {
      this.observer = observer;
      this.event = event;
   }

   /**
    * @return the observer
    */
   public final Observer<?> getObserver()
   {
      return observer;
   }

   public void afterCompletion(int arg0)
   {
      // The event is already delivered before completion
   }

   public void beforeCompletion()
   {
      // Execute the observer method on the event
      // TODO It is impossible to use a template variable for this observer, so notify cannot be called
      //observer.notify(event);      
   }

}
