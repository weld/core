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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.webbeans.ApplicationScoped;

/**
 * The Application context
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.webbeans.contexts.ApplicationContext
 */
public class ApplicationContext extends AbstractContext
{

   public static ApplicationContext INSTANCE = new ApplicationContext();
   
   // The beans
   private BeanMap beanMap;
   // Is the context active?
   private AtomicBoolean active;
   
   /**
    * Constructor
    */
   protected ApplicationContext()
   {
      super(ApplicationScoped.class);
      this.active = new AtomicBoolean(true);
   }

   /**
    * Gets the bean map
    * 
    * @return The bean map
    */
   @Override
   public BeanMap getBeanMap()
   {
      return this.beanMap;
   }
   
   /**
    * Sets the bean map
    * 
    * @param applicationBeanMap The bean map
    */
   public void setBeanMap(BeanMap applicationBeanMap)
   {
      this.beanMap = applicationBeanMap;
   }
   
   /**
    * Indicates if the context is active
    * 
    * @return True if active, false otherwise
    */
   @Override
   public boolean isActive()
   {
      return active.get();
   }
   
   /**
    * Sets the active state of the context
    * 
    * @param active The new state
    */
   @Override
   public void setActive(boolean active)
   {
      this.active.set(active);
   }
   
}
