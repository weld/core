package org.jboss.webbeans.test;

import java.util.Set;

import javax.transaction.Synchronization;
import javax.webbeans.Observer;

import org.jboss.webbeans.event.DeferredEventNotification;
import org.jboss.webbeans.event.EventManager;
import org.jboss.webbeans.test.beans.DangerCall;
import org.jboss.webbeans.test.bindings.TameAnnotationLiteral;
import org.testng.annotations.Test;

/**
 * Tests for the EventManager implementation used by the Web Beans RI.
 * 
 * @author David Allen
 * 
 */
@SpecVersion("PDR")
public class EventManagerTest extends AbstractTest
{
   public class AnObserver<T> implements Observer<T>
   {
      public void notify(T event)
      {
      }
   }

   private Synchronization registeredSynch;

   /**
    * Tests adding an observer to the event bus and verified that it can still
    * be retrieved for a corresponding event.
    */
   @Test(groups = "observerMethod")
   public void testAddObserver()
   {
      EventManager eventManager = new EventManager(manager);
      Observer<DangerCall> observer = new AnObserver<DangerCall>();
      eventManager.addObserver(observer, DangerCall.class);
      DangerCall event = new DangerCall();

      Set<Observer<DangerCall>> observerSet = eventManager.getObservers(event);
      assert observerSet.size() == 1;
      assert observerSet.iterator().next().equals(observer);

      // Add another observer for the same event, but with an event binding
      observer = new AnObserver<DangerCall>();
      eventManager.addObserver(observer, DangerCall.class, new TameAnnotationLiteral());
      observerSet = eventManager.getObservers(event);
      assert observerSet.size() == 1;
      observerSet = eventManager.getObservers(event, new TameAnnotationLiteral());
      assert observerSet.size() == 2;
   }

   /**
    * Tests the remove operation and verifies that the observer is no longer
    * registered for events.
    */
   @Test(groups = { "observerMethod" })
   public void testRemoveObserver()
   {
      EventManager eventManager = new EventManager(manager);
      Observer<DangerCall> observer = new AnObserver<DangerCall>();
      eventManager.addObserver(observer, DangerCall.class);
      eventManager.removeObserver(observer, DangerCall.class);
      // FIXME CopyOnWrite broke remove, have to check later
      assert eventManager.getObservers(new DangerCall()).isEmpty();
   }

   /**
    * Tests the deferred event feature associated with transactions.
    */
   @Test(groups = { "deferredEvent", "broken" })
   public void testDeferEvent()
   {
      // Setup a transaction manager for this test and inject into the event bus
      // TransactionManager tm = new TransactionManager() {
      // public void begin() throws NotSupportedException, SystemException
      // {
      // }
      //
      // public void commit() throws RollbackException,
      // HeuristicMixedException, HeuristicRollbackException,
      // SecurityException, IllegalStateException, SystemException
      // {
      // }
      //
      // public int getStatus() throws SystemException
      // {
      // return 0;
      // }
      //
      // public Transaction getTransaction() throws SystemException
      // {
      // return new Transaction() {
      //               
      // public void commit() throws RollbackException,
      // HeuristicMixedException, HeuristicRollbackException,
      // SecurityException, IllegalStateException, SystemException
      // {
      // }
      //
      // public boolean delistResource(XAResource arg0, int arg1)
      // throws IllegalStateException, SystemException
      // {
      // return false;
      // }
      //
      // public boolean enlistResource(XAResource arg0)
      // throws RollbackException, IllegalStateException,
      // SystemException
      // {
      // return false;
      // }
      //
      // public int getStatus() throws SystemException
      // {
      // return 0;
      // }
      //
      // public void registerSynchronization(Synchronization synchronization)
      // throws RollbackException, IllegalStateException,
      // SystemException
      // {
      // registeredSynch = synchronization;
      // }
      //
      // public void rollback() throws IllegalStateException,
      // SystemException
      // {
      // }
      //
      // public void setRollbackOnly() throws IllegalStateException,
      // SystemException
      // {
      // }
      //               
      // };
      // }
      //
      // public void resume(Transaction arg0)
      // throws InvalidTransactionException, IllegalStateException,
      // SystemException
      // {
      // }
      //
      // public void rollback() throws IllegalStateException,
      // SecurityException, SystemException
      // {
      // }
      //
      // public void setRollbackOnly() throws IllegalStateException,
      // SystemException
      // {
      // }
      //
      // public void setTransactionTimeout(int arg0) throws SystemException
      // {
      // }
      //
      // public Transaction suspend() throws SystemException
      // {
      // return null;
      // }
      //         
      // };
      EventManager eventManager = new EventManager(manager);
      Observer<DangerCall> observer = new AnObserver<DangerCall>();
      try
      {
//         eventManager.deferEvent(new DangerCall(), observer);
      }
      catch (Exception e)
      {
      }

      assert this.registeredSynch != null;
      assert ((DeferredEventNotification) this.registeredSynch).getObserver().equals(observer);
   }
}
