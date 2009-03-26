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
package org.jboss.webbeans.bootstrap.api;

import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.manager.api.WebBeansManager;

/**
 * Bootstrap API for Web Beans.
 * 
 * @author Pete Muir
 * 
 */
public interface Bootstrap
{
   
   /**
    * Set the bean store to use as backing for the application context
    * 
    * @param beanStore the bean store to use
    */
   public void setApplicationContext(BeanStore beanStore);

   /**
    * Set the environment in use, by default {@link Environments.EE}
    * 
    * @param environment the environment to use
    */
   public void setEnvironment(Environment environment);
   
   /**
    * Initialize the bootstrap:
    * <ul>
    * <li>Create the manager and bind it to JNDI</li>
    * </ul>
    * 
    * @throws IllegalStateException
    *            if not all the services required for the given environment are
    *            available
    */
   public void initialize();
   
   /**
    * Get the manager used for this application.
    * 
    * @return the manager. Unless {@link #initialize()} has been called, this
    *         method will return null.
    */
   public WebBeansManager getManager();
   
   /**
    * Starts the boot process.
    * 
    * Discovers the beans and registers them with the getManager(). Also
    * resolves the injection points. Before running {@link #boot()} 
    * {@link #initialize()} must have been called and the contexts should be 
    * available
    * 
    */
   public void boot();
   
   /**
    * Causes the container to clean up and shutdown
    * 
    */
   public void shutdown();
   
   /**
    * Get the services available to this bootstrap
    * 
    * @return the services available
    */
   public ServiceRegistry getServices();
   
}
