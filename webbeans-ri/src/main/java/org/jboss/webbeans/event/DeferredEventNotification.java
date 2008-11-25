package org.jboss.webbeans.event;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.webbeans.Observer;

/**
 * A synchronization object which will deliver the event to the observer after
 * the JTA transaction currently in effect is committed.
 * 
 * @author David Allen
 * 
 */
public class DeferredEventNotification<T> implements Synchronization
{
   private ObserverImpl<T> observer;
   private T event;

   /**
    * Creates a new deferred event notifier.
    * 
    * @param manager The Web Beans manager
    * @param observer The observer to be notified
    * @param event The event being fired
    */
   public DeferredEventNotification(T event, Observer<T> observer)
   {
      this.observer = (ObserverImpl<T>) observer;
      this.event = event;
   }

   /**
    * @return the observer
    */
   public final Observer<T> getObserver()
   {
      return observer;
   }

   public void afterCompletion(int status)
   {
      if (observer.isInterestedInTransactionPhase(TransactionObservationPhase.AFTER_COMPLETION))
      {
         observer.notify(event);
      }
      switch (status)
      {
      case Status.STATUS_COMMITTED:
         if (observer.isInterestedInTransactionPhase(TransactionObservationPhase.AFTER_SUCCESS))
         {
            observer.notify();
         }
         break;
      case Status.STATUS_ROLLEDBACK:
         if (observer.isInterestedInTransactionPhase(TransactionObservationPhase.AFTER_FAILURE))
         {
            observer.notify();
         }
         break;
      }
   }

   public void beforeCompletion()
   {
      if (observer.isInterestedInTransactionPhase(TransactionObservationPhase.BEFORE_COMPLETION))
      {
         observer.notify(event);
      }
   }
}
