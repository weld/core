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

package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The abstraction of a private context, on that operates on a ThreadLocal
 * BeanMap and ThreadLocal active state
 * 
 * A private context doesn't rely on some external context to hold it's state
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.webbeans.contexts.DependentContext
 * @see org.jboss.webbeans.contexts.RequestContext
 * @see org.jboss.webbeans.contexts.ConversationContext
 * @see org.jboss.webbeans.contexts.SessionContext
 */
public class PrivateContext extends AbstractContext
{
   private ThreadLocal<AtomicBoolean> active;
   protected ThreadLocal<BeanMap> beans;

   public PrivateContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new ThreadLocal<BeanMap>()
      {
         
         @Override
         protected BeanMap initialValue()
         {
            return new SimpleBeanMap();
         }
         
      };
      active = new ThreadLocal<AtomicBoolean>();
      active.set(new AtomicBoolean(true));
   }

   /**
    * Delegates to a ThreadLocal instance
    */
   @Override
   protected AtomicBoolean getActive()
   {
      return active.get();
   }

   /**
    * Delegates to a ThreadLocal instance
    */
   @Override
   protected BeanMap getBeanMap()
   {
      return beans.get();
   }

}
