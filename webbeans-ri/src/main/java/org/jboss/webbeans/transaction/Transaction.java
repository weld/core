package org.jboss.webbeans.transaction;

import javax.webbeans.Current;
import javax.webbeans.Produces;

import org.jboss.webbeans.ManagerImpl;

/**
 * Transaction manager component
 * 
 * @author Pete Muir
 *
 */
public class Transaction
{
 
   public static final String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";
   
   @Current ManagerImpl manager;
   
   @Produces
   public UserTransaction getCurrentTransaction()
   {
      return new UTTransaction(manager.getNaming().lookup(USER_TRANSACTION_JNDI_NAME, javax.transaction.UserTransaction.class));
   }
   
}
