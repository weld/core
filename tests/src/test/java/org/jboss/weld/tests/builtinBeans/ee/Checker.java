package org.jboss.weld.tests.builtinBeans.ee;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class Checker
{
   
   public static boolean checkUserTransaction(UserTransaction userTransaction)
   {
      try
      {
         if (userTransaction != null)
         {
            userTransaction.getStatus();
            return true;
         }
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
      return false;
   }
   
   
   public static boolean checkEntityManager(EntityManager entityManager)
   {
      if (entityManager != null)
      {
         return entityManager.isOpen();
      }
      else
      {
         return false;
      }
   }
   
   public static boolean checkEntityManagerFactory(EntityManagerFactory entityManagerFactory)
   {
      if (entityManagerFactory != null)
      {
         return entityManagerFactory.isOpen();
      }
      else
      {
         return false;
      }
   }
   
   public static boolean checkRemoteEjb(HorseRemote horse)
   {
      if (horse != null)
      {
         return horse.ping();
      }
      else
      {
         return false;
      }
   }
   
}
