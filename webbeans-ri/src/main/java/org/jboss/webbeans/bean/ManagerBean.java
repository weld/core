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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.util.Reflections;

/**
 * Helper bean for accessing the Manager
 * 
 * @author Pete Muir
 * 
 */
public class ManagerBean extends Bean<Manager>
{
   // The API types of the manager
   private static Set<Class<?>> types = Reflections.getTypeHierachy(Manager.class);
   // The binding types of the manager
   private static final Set<Annotation> BINDING = new HashSet<Annotation>(Arrays.asList(new CurrentBinding()));

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    */
   public ManagerBean(Manager manager)
   {
      super(manager);
   }

   /**
    * Creates an instance of the manager
    * 
    * @return An instance
    */
   @Override
   public Manager create()
   {
      return getManager();
   }

   /**
    * Destroys the manager (not)
    */
   @Override
   public void destroy(Manager instance)
   {
      // No -op
   }

   /**
    * Gets the binding types
    * 
    * @return A set containing Current
    */
   @Override
   public Set<Annotation> getBindingTypes()
   {
      return BINDING;
   }

   /**
    * Gets the deployment types
    * 
    * @return Standard
    */
   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return Standard.class;
   }

   /**
    * Gets the name
    * 
    * @return null
    */
   @Override
   public String getName()
   {
      return null;
   }

   /**
    * Gets the scope type
    * 
    * @return Dependent
    */
   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return Dependent.class;
   }

   /**
    * Gets the API types
    * 
    * @return The API types
    */
   @Override
   public Set<Class<?>> getTypes()
   {
      return types;
   }

   /**
    * Indicates if the bean is nullable
    * 
    * @return true
    */
   @Override
   public boolean isNullable()
   {
      return true;
   }

   /**
    * Indicates if the bean is serializable
    * 
    * @return false
    */
   @Override
   public boolean isSerializable()
   {
      return false;
   }

}