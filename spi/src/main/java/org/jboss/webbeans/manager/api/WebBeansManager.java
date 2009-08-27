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

package org.jboss.webbeans.manager.api;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * Functionality provided by the Web Beans Manager over and above the JSR-299
 * Manager.
 * 
 * @author Pete Muir
 *
 */
public interface WebBeansManager extends BeanManager, Serializable
{

   public void shutdown();

   /**
    * Create a new child activity. A child activity inherits all beans,
    * interceptors, decorators, observers, and contexts defined by its direct
    * and indirect parent activities.
    * 
    * This method should not be called by the application.
    * 
    * @return the child activity
    */
   public WebBeansManager createActivity();

   /**
    * Associate an activity with the current context for a normal scope
    * 
    * @param scopeType
    *           the scope to associate the activity with
    * @return the activity
    * @throws ContextNotActiveException
    *            if the given scope is inactive
    * @throws IllegalArgumentException
    *            if the given scope is not a normal scope
    */
   public WebBeansManager setCurrent(Class<? extends Annotation> scopeType);
   
   /**
    * The injection target for the given EJB, or null if Web Beans was not
    * given this descriptor in the deployment.
    * 
    * @param <T>
    * @param descriptor
    * @return
    */
   public <T> InjectionTarget<T> createInjectionTarget(EjbDescriptor<T> descriptor);
   
   public <T> Bean<T> getBean(EjbDescriptor<T> descriptor);
   
   public <T> EjbDescriptor<T> getEjbDescriptor(String beanName);

}
