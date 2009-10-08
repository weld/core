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
package org.jboss.weld.bean.builtin;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.transaction.UserTransaction;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.collections.Arrays2;

/**
 * @author pmuir
 *
 */
public class UserTransactionBean extends AbstractBuiltInBean<UserTransaction>
{

   private static final Set<Type> TYPES = Arrays2.<Type>asSet(Object.class, UserTransaction.class);
   
   public UserTransactionBean(BeanManagerImpl manager)
   {
      super(UserTransaction.class.getSimpleName(), manager);
   }

   @Override
   public Class<UserTransaction> getType()
   {
      return UserTransaction.class;
   }

   public Set<Type> getTypes()
   {
      return TYPES;
   }

   public UserTransaction create(CreationalContext<UserTransaction> creationalContext)
   {
      if (getManager().getServices().contains(TransactionServices.class))
      {
         return getManager().getServices().get(TransactionServices.class).getUserTransaction();
      }
      else
      {
         throw new IllegalStateException("TransactionServices not available");
      }
   }

   public void destroy(UserTransaction instance, CreationalContext<UserTransaction> creationalContext)
   {
      // No-op      
   }

}
