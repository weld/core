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
package org.jboss.webbeans.context;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Context;

/**
 * Common Context operation
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 */
public abstract class AbstractContext implements Context
{
   // The scope type
   private Class<? extends Annotation> scopeType;
   // The active state of the context
   private ThreadLocal<Boolean> active;

   /**
    * Constructor
    * 
    * @param scopeType The scope type
    */
   public AbstractContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      this.active = new ThreadLocal<Boolean>()
      {
         @Override
         protected Boolean initialValue()
         {
            return Boolean.FALSE;
         }
      };
   }

   /**
    * Get the scope the context is for
    * 
    * @return The scope type
    * 
    * @see javax.enterprise.context.spi.Context#getScope()
    */
   public Class<? extends Annotation> getScope()
   {
      return scopeType;
   }

   /**
    * Return true if the context is active
    * 
    * @return The active state
    * 
    * @see javax.enterprise.context.spi.Context#isActive()
    */
   public boolean isActive()
   {
      return active.get().booleanValue();
   }

   /**
    * Set the context active, internal API for WBRI
    * 
    * @param active The new state
    */
   public void setActive(boolean active)
   {
      this.active.set(Boolean.valueOf(active));
   }
   
   public void cleanup()
   {
      this.active.remove();
   }

}
