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

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.util.Reflections;

/**
 * Helper bean for accessing the Manager
 * 
 * @author Gavin King
 *
 */
public class ManagerBean extends Bean<Manager>
{
   
   private static Set<Class<?>> types = Reflections.getTypeHierachy(Manager.class);
   private static final Set<Annotation> BINDING = new HashSet<Annotation>(Arrays.asList(new CurrentAnnotationLiteral()));

   public ManagerBean(Manager manager)
   {
      super(manager);
   }
   
   @Override
   public Manager create()
   {
      return getManager();
   }

   @Override
   public void destroy(Manager instance)
   {
      //No -op
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return BINDING;
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return Standard.class;
   }

   @Override
   public String getName()
   {
      return null;
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return Dependent.class;
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      return types;
   }

   @Override
   public boolean isNullable()
   {
      return true;
   }

   @Override
   public boolean isSerializable()
   {
      return false;
   }
   
}