package org.jboss.webbeans.bootstrap.api.test;

import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;

import org.jboss.webbeans.transaction.spi.TransactionServices;

public class MockTransactionServices implements TransactionServices
{
   
   public boolean isTransactionActive()
   {
      return false;
   }
   
   public void registerSynchronization(Synchronization synchronizedObserver)
   {
   }
   
   public UserTransaction getUserTransaction()
   {
      return null;
   }
   
}
