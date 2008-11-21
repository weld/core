package org.jboss.webbeans.transaction;

import java.rmi.RemoteException;
import java.util.LinkedList;

import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.Synchronization;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class EjbSynchronizations implements LocalEjbSynchronizations, SessionSynchronization
{
   protected LinkedList<SynchronizationRegistry> synchronizations = new LinkedList<SynchronizationRegistry>();
   protected LinkedList<SynchronizationRegistry> committing = new LinkedList<SynchronizationRegistry>();

   public void afterBegin()
   {
      synchronizations.addLast(new SynchronizationRegistry());
   }

   public void beforeCompletion() throws EJBException, RemoteException
   {
      SynchronizationRegistry synchronization = synchronizations.removeLast();
      synchronization.beforeTransactionCompletion();
      committing.addLast(synchronization);
   }

   public void afterCompletion(boolean success) throws EJBException, RemoteException
   {
      if (committing.isEmpty())
      {
         if (success)
         {
            throw new IllegalStateException("beforeCompletion was never called");
         }
         else
         {
            synchronizations.removeLast().afterTransactionCompletion(false);
         }
      }
      else
      {
         committing.removeFirst().afterTransactionCompletion(success);
      }
   }

   public boolean isAwareOfContainerTransactions()
   {
      return true;
   }

   public void afterTransactionBegin()
   {
      // noop, let JTA notify us
   }

   public void afterTransactionCommit(boolean success)
   {
      // noop, let JTA notify us
   }

   public void afterTransactionRollback()
   {
      // noop, let JTA notify us
   }

   public void beforeTransactionCommit()
   {
      // noop, let JTA notify us
   }

   public void registerSynchronization(Synchronization synchronization)
   {
      synchronizations.getLast().registerSynchronization(synchronization);
   }

   @Remove
   public void destroy()
   {
   }

}
