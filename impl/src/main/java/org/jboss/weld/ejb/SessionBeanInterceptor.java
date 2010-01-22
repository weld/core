/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.ejb;

import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.beanstore.HashMapBeanStore;

/**
 * Interceptor for ensuring the request context is active during requests to EJBs.
 * 
 * Normally, a servlet will start the request context, however in non-servlet 
 * requests (e.g. MDB, async, timeout) the contexts may need starting.
 * 
 * The Application context is active for duration of the deployment
 * 
 * @author Pete Muir
 */
public class SessionBeanInterceptor implements Serializable
{
   private static final long serialVersionUID = 7327757031821596782L;

   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      if (Container.instance().services().get(ContextLifecycle.class).isRequestActive())
      {
         return invocation.proceed();
      }
      else
      {
         Lifecycle lifecycle = Container.instance().services().get(ContextLifecycle.class);
         BeanStore beanStore = new HashMapBeanStore();
         String id = invocation.getTarget().getClass().getName() + "." + invocation.getMethod().getName() + "()";
         lifecycle.beginRequest(id, beanStore);
         try
         {
            return invocation.proceed();
         }
         finally
         {
            lifecycle.endRequest(id, beanStore);
         }
      }
   }
   
}

   