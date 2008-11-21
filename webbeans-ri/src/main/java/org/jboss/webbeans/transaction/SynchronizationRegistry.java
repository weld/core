package org.jboss.webbeans.transaction;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.Synchronization;

class SynchronizationRegistry
{
   private List<Synchronization> synchronizations = new ArrayList<Synchronization>();

   void registerSynchronization(Synchronization sync)
   {
      synchronizations.add(sync);
   }

   void afterTransactionCompletion(boolean success)
   {
      for (Synchronization synchronization : synchronizations)
      {
         try
         {
            synchronization.afterCompletion(success ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      synchronizations.clear();
   }

   void beforeTransactionCompletion()
   {
      for (Synchronization synchronization : synchronizations)
      {
         try
         {
            synchronization.beforeCompletion();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

}
