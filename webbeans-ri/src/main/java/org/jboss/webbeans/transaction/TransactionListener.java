package org.jboss.webbeans.transaction;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class TransactionListener implements LocalTransactionListener, SessionSynchronization
{

   private List<Synchronization> synchronizations = new ArrayList<Synchronization>();

   public void afterBegin() throws EJBException, RemoteException
   {
   }

   public void afterCompletion(boolean success) throws EJBException, RemoteException
   {
      for (Synchronization synchronization : synchronizations)
      {
         synchronization.afterCompletion(success ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK);
      }
      synchronizations.clear();
   }

   public void beforeCompletion() throws EJBException, RemoteException
   {
      for (Synchronization synchronization : synchronizations)
      {
         synchronization.beforeCompletion();
      }
   }

   public void registerSynhronization(Synchronization synchronization)
   {
      synchronizations.add(synchronization);
   }

   @Remove
   public void destroy()
   {      
   }
}
