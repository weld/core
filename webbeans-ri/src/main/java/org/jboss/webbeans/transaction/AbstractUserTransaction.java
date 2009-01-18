/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans.transaction;

import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_COMMITTED;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;
import static javax.transaction.Status.STATUS_ROLLEDBACK;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;

/**
 * Base implementation of UserTransaction
 * 
 * @author Gavin King
 * 
 */
public abstract class AbstractUserTransaction implements UserTransaction
{
   
   public boolean isActive() throws SystemException
   {
      return getStatus() == STATUS_ACTIVE;
   }

   public boolean isActiveOrMarkedRollback() throws SystemException
   {
      int status = getStatus();
      return status == STATUS_ACTIVE || status == STATUS_MARKED_ROLLBACK;
   }

   public boolean isRolledBackOrMarkedRollback() throws SystemException
   {
      int status = getStatus();
      return status == STATUS_ROLLEDBACK || status == STATUS_MARKED_ROLLBACK;
   }

   public boolean isMarkedRollback() throws SystemException
   {
      return getStatus() == STATUS_MARKED_ROLLBACK;
   }

   public boolean isNoTransaction() throws SystemException
   {
      return getStatus() == STATUS_NO_TRANSACTION;
   }

   public boolean isRolledBack() throws SystemException
   {
      return getStatus() == STATUS_ROLLEDBACK;
   }

   public boolean isCommitted() throws SystemException
   {
      return getStatus() == STATUS_COMMITTED;
   }
   
   public boolean isConversationContextRequired()
   {
      return false;
   }
   
   public abstract void registerSynchronization(Synchronization sync);
   
   /*public void enlist(EntityManager entityManager) throws SystemException
   {
      if ( isActiveOrMarkedRollback() )
      {
         entityManager.joinTransaction();
      }
   }*/
      
}
