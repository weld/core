package org.jboss.webbeans.transaction.spi;

import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;

import org.jboss.webbeans.bootstrap.api.Service;

/**
 * <p>
 * The container must implement the services related to transactional behavior
 * used in JSR-299, if that behavior is going to be used.
 * </p>
 * 
 * <p>
 * The event framework specified by CDI includes the ability to create observer 
 * methods which are activated based on the phase and status of a currently 
 * active transaction. In order to use these abilities, the container must 
 * provide these intermediary services which in turn may interact with an 
 * application server and JTA.
 * </p>
 * 
 * <p>Required in a Java EE environment</p>
 * 
 * @author David Allen
 * 
 */
public interface TransactionServices extends Service
{
   /**
    * Registers a synchronization object with the currently executing
    * transaction.
    * 
    * @see javax.transaction.Synchronization
    * @param synchronizedObserver
    */
   public void registerSynchronization(Synchronization synchronizedObserver);

   /**
    * Queries the status of the current execution to see if a transaction is
    * currently active.
    * 
    * @return true if a transaction is active
    */
   public boolean isTransactionActive();
   
   /**
    * Obtain a reference to the JTA UserTransaction
    * 
    * @return a reference to the JTA UserTransaction
    */
   public UserTransaction getUserTransaction();
}
