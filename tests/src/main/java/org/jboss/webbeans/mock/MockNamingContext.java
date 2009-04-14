/**
 * 
 */
package org.jboss.webbeans.mock;

import javax.naming.Context;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.webbeans.resources.spi.helpers.AbstractNamingContext;

public class MockNamingContext extends AbstractNamingContext
{
   
   private final Context context;
   
   public MockNamingContext(Context context)
   {
      this.context = context;
   }

   public void bind(String key, Object value)
   {
      if (context != null)
      {
         super.bind(key, value);
      }
   }

   public void unbind(String key)
   {
      if (context != null)
      {
         super.unbind(key);
      }
   }

   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      if (context != null)
      {
         T instance = overrideLookup(name, expectedType);
         if (instance == null)
         {
            instance = super.lookup(name, expectedType);
         }
         return instance;
      }
      else
      {
         return null;
      }
   }
   
   @SuppressWarnings("unchecked")
   private <T> T overrideLookup(String name, Class<? extends T> expectedType)
   {
      // JBoss Embedded EJB 3.1 doesn't seem to bind this!
      if (name.equals("java:comp/UserTransaction"))
      {
         final TransactionManager tm = super.lookup("java:/TransactionManager", TransactionManager.class);
         return (T) new UserTransaction()
         {

            public void begin() throws NotSupportedException, SystemException
            {
               tm.begin();
            }

            public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
            {
               tm.commit();
            }

            public int getStatus() throws SystemException
            {
               return tm.getStatus();
            }

            public void rollback() throws IllegalStateException, SecurityException, SystemException
            {
               tm.rollback();
            }

            public void setRollbackOnly() throws IllegalStateException, SystemException
            {
               tm.setRollbackOnly();
            }

            public void setTransactionTimeout(int seconds) throws SystemException
            {
               tm.setTransactionTimeout(seconds);
            }
            
         };
      }
      else
      {
         return null;
      }
   }

   @Override
   public Context getContext()
   {
      return context;
   }
   
}