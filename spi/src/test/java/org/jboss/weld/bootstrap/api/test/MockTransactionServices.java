package org.jboss.weld.bootstrap.api.test;

import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;

import org.jboss.weld.transaction.spi.TransactionServices;

public class MockTransactionServices extends MockService implements TransactionServices
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
