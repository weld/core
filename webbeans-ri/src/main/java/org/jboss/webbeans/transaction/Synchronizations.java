package org.jboss.webbeans.transaction;

import javax.transaction.Synchronization;

public interface Synchronizations
{
   public void afterTransactionBegin();
   public void afterTransactionCommit(boolean success);
   public void afterTransactionRollback();
   public void beforeTransactionCommit();
   public void registerSynchronization(Synchronization sync);
   public boolean isAwareOfContainerTransactions();
}