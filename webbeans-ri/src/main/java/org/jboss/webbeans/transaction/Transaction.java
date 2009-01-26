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

import javax.inject.Current;
import javax.inject.Produces;

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
