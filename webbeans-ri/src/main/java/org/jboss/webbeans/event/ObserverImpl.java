package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.AfterTransactionCompletion;
import javax.webbeans.AfterTransactionFailure;
import javax.webbeans.AfterTransactionSuccess;
import javax.webbeans.BeforeTransactionCompletion;
import javax.webbeans.IfExists;
import javax.webbeans.Observer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.introspector.AnnotatedMethod;

/**
 * <p>
 * Reference implementation for the Observer interface, which represents an
 * observer method. Each observer method has an event type which is the class of
 * the event object being observed, and event binding types that are annotations
 * applied to the event parameter to narrow the event notifications delivered.
 * </p>
 * 
 * @author David Allen
 * 
 */
public class ObserverImpl<T> implements Observer<T>
{
   private EventBean<T> eventBean;
   private final AnnotatedMethod<Object> observerMethod;
   private final Class<T> eventType;
   private Set<TransactionObservationPhase> transactionObservationPhases;
   private boolean conditional;
   private ManagerImpl manager;

   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (7.5).
    * 
    * @param componentModel The model for the component which defines the
    *           observer method
    * @param observer The observer method to notify
    * @param eventType The type of event being observed
    * @param beanModel The model for the bean which defines the observer method
    * @param observer The observer method to notify
    * @param eventType The type of event being observed
    */
   public ObserverImpl(ManagerImpl manager, final EventBean<T> eventBean, final AnnotatedMethod<Object> observer, final Class<T> eventType)
   {
      this.manager = manager;
      this.eventBean = eventBean;
      this.observerMethod = observer;
      this.eventType = eventType;
      initTransactionObservationPhases();
      conditional = !observerMethod.getAnnotatedParameters(IfExists.class).isEmpty();
   }

   private void initTransactionObservationPhases()
   {
      transactionObservationPhases = new HashSet<TransactionObservationPhase>();
      if (observerMethod.getAnnotatedParameters(BeforeTransactionCompletion.class).isEmpty())
      {
         transactionObservationPhases.add(TransactionObservationPhase.BEFORE_COMPLETION);
      }
      if (observerMethod.getAnnotatedParameters(AfterTransactionCompletion.class).isEmpty())
      {
         transactionObservationPhases.add(TransactionObservationPhase.AFTER_COMPLETION);
      }
      if (observerMethod.getAnnotatedParameters(AfterTransactionFailure.class).isEmpty())
      {
         transactionObservationPhases.add(TransactionObservationPhase.AFTER_FAILURE);
      }
      if (observerMethod.getAnnotatedParameters(AfterTransactionSuccess.class).isEmpty())
      {
         transactionObservationPhases.add(TransactionObservationPhase.AFTER_SUCCESS);
      }
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
      Object instance = getInstance(isConditional());
      if (instance != null)
      {
         // TODO replace event parameter
         observerMethod.invoke(manager, instance);
      }

   }

   /**
    * Uses the container to retrieve the most specialized instance of this
    * observer.
    * 
    * @param conditional
    * 
    * @return the most specialized instance
    */
   protected Object getInstance(boolean conditional)
   {
      // Return the most specialized instance of the component
      return manager.getInstanceByType(eventBean.getType(), eventBean.getBindingTypes().toArray(new Annotation[0]));
   }

   public boolean isTransactional()
   {
      return !transactionObservationPhases.isEmpty();
   }

   public boolean isConditional()
   {
      return conditional;
   }

   public boolean isInterestedInTransactionPhase(TransactionObservationPhase transactionObservationPhase)
   {
      return transactionObservationPhases.contains(transactionObservationPhase);
   }

}
