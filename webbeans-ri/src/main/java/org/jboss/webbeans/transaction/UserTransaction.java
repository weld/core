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
