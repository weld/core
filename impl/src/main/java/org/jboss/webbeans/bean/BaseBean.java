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

package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * The contract between the manager and a bean. This interface
 * should not be called directly by the application.
 * 
 * @author Gavin King
 * 
 * @param <T> an API type of the bean
 */
public abstract class BaseBean<T> implements Bean<T>
{
   private final BeanManager beanManager;

   /**
    * Create an instance of a bean
    * 
    * @param beanManager
    */
   protected BaseBean(BeanManager beanManager)
   {
      this.beanManager = beanManager;
   }

   /**
    * Get the manager used to create this bean
    * 
    * @return an instance of the manager
    */
   protected BeanManager getManager()
   {
      return beanManager;
   }

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#getTypes()
    */
   public abstract Set<Type> getTypes();

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#getBindings()
    */
   public abstract Set<Annotation> getBindings();

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#getScopeType()
    */
   public abstract Class<? extends Annotation> getScopeType();

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#getDeploymentType()
    */
   public abstract Class<? extends Annotation> getDeploymentType();

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#getName()
    */
   public abstract String getName();

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#isSerializable()
    */
   public abstract boolean isSerializable();

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#isNullable()
    */
   public abstract boolean isNullable();

   /* (non-Javadoc)
    * @see javax.inject.manager.Bean#getInjectionPoints()
    */
   public abstract Set<InjectionPoint> getInjectionPoints();

}
