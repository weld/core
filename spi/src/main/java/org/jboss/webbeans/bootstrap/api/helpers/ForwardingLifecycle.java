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
package org.jboss.webbeans.bootstrap.api.helpers;

import javax.enterprise.context.spi.Context;

import org.jboss.webbeans.bootstrap.api.Lifecycle;
import org.jboss.webbeans.context.api.BeanStore;

/**
 * @author pmuir
 *
 */
public abstract class ForwardingLifecycle implements Lifecycle
{
   
   protected abstract Lifecycle delegate();

   public void beginApplication(BeanStore applicationBeanStore)
   {
      delegate().beginApplication(applicationBeanStore);
   }

   public void beginRequest(String id, BeanStore requestBeanStore)
   {
      delegate().beginRequest(id, requestBeanStore);
   }

   public void endApplication()
   {
      delegate().endApplication();
   }

   public void endRequest(String id, BeanStore requestBeanStore)
   {
      delegate().endRequest(id, requestBeanStore);
   }

   public void endSession(String id, BeanStore sessionBeanStore)
   {
      delegate().endSession(id, sessionBeanStore);
   }

   public Context getApplicationContext()
   {
      return delegate().getApplicationContext();
   }

   public Context getConversationContext()
   {
      return delegate().getConversationContext();
   }

   public Context getDependentContext()
   {
      return delegate().getDependentContext();
   }

   public Context getRequestContext()
   {
      return delegate().getRequestContext();
   }

   public Context getSessionContext()
   {
      return delegate().getSessionContext();
   }

   public boolean isRequestActive()
   {
      return delegate().isRequestActive();
   }
   
   public boolean isApplicationActive()
   {
      return delegate().isApplicationActive();
   }
   
   public boolean isConversationActive()
   {
      return delegate().isConversationActive();
   }
   
   public boolean isSessionActive()
   {
      return delegate().isSessionActive();
   }

   public void restoreSession(String id, BeanStore sessionBeanStore)
   {
      delegate().restoreSession(id, sessionBeanStore);
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return this == obj || delegate().equals(obj);
   }

}
