package org.jboss.webbeans.transaction;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;

/**
 * Extends the standard UserTransaction interface with a couple 
 * of helpful methods.
 * 
 * @author Gavin King
 * 
 */
public interface UserTransaction extends javax.transaction.UserTransaction
{
   
   public boolean isActive() throws SystemException;
   public boolean isActiveOrMarkedRollback() throws SystemException;
   public boolean isRolledBackOrMarkedRollback() throws SystemException;
   public boolean isMarkedRollback() throws SystemException;
   public boolean isNoTransaction() throws SystemException;
   public boolean isRolledBack() throws SystemException;
   public boolean isCommitted() throws SystemException;
 
   public boolean isConversationContextRequired();
   public abstract void registerSynchronization(Synchronization sync);

   // public void enlist(EntityManager entityManager) throws SystemException;
}
