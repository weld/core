package org.jboss.webbeans.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.webbeans.Produces;

public class Transaction
{

   @Produces
   public UserTransaction getCurrentTransaction()
   {
      return new UTTransaction(new javax.transaction.UserTransaction()
      {
         
         public void begin() throws NotSupportedException, SystemException
         {
            // TODO Auto-generated method stub
            
         }
         
         public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
         {
            // TODO Auto-generated method stub
            
         }
         
         public int getStatus() throws SystemException
         {
            return javax.transaction.Status.STATUS_UNKNOWN;
         }
         
         public void rollback() throws IllegalStateException, SecurityException, SystemException
         {
            // TODO Auto-generated method stub
            
         }
         
         public void setRollbackOnly() throws IllegalStateException, SystemException
         {
            // TODO Auto-generated method stub
            
         }
         
         public void setTransactionTimeout(int arg0) throws SystemException
         {
            // TODO Auto-generated method stub
            
         }
         
         
         
      });
   }
   
}
