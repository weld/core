package org.jboss.webbeans.transaction.spi;

/**
 * <p>
 * The container must implement the services related to transactional behavior
 * used in JSR-299, if that behavior is going to be used.
 * </p>
 * 
 * <p>
 * The event framework specified by JSR-299 includes the ability to create
 * observer methods which are activated based on the phase and status of a
 * currently active transaction. In order to use these abilities, the container
 * must provide these intermediary services which in turn may interact with an
 * application server and JTA or any other type of transaction service provider.
 * </p>
 * 
 * @author David Allen
 * 
 */
public interface TransactionServices
{
   /**
    * Possible status conditions for a transaction.
    */
   public static enum Status
   {
      ALL, SUCCESS, FAILURE
   }

   /**
    * Registers a task to be executed immediately before the current transaction
    * is committed or rolled back.
    * 
    * @param task The Runnable that will be executed
    */
   public void executeBeforeTransactionCompletion(Runnable task);

   /**
    * Registers a task to be executed immediately after the current transaction
    * is committed or rolled back.
    * 
    * @param task The Runnable that will be executed
    */
   public void executeAfterTransactionCompletion(Runnable task);

   /**
    * Registers a task to be executed immediately after the current transaction
    * is committed or rolled back, but only one depending on the status
    * provided.
    * 
    * @param task The Runnable that will be executed
    */
   public void executeAfterTransactionCompletion(Runnable task, Status desiredStatus);
   
   /**
    * Queries the status of the current execution to see if a transaction is
    * currently active.
    * 
    * @return true if a transaction is active
    */
   public boolean isTransactionActive();
}
