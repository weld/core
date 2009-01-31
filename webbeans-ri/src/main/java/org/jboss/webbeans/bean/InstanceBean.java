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


import javax.context.CreationalContext;
import javax.inject.Instance;

import org.jboss.webbeans.InstanceImpl;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.introspector.AnnotatedItem;

/**
 * Helper bean for accessing instances
 * 
 * @author Gavin King
 *
 * @param <T>
 * @param <S>
 */
public class InstanceBean<T, S> extends AbstractFacadeBean<Instance<T>, S, T>
{
   
   /**
    * Creates an instance Web Bean
    * 
    * @param item The instance injection point abstraction
    * @param manager the current manager
    * @param declaringBean The declaring bean abstraction
    * @return An event Web Bean
    */
   public static <T, S> InstanceBean<T, S> of(AnnotatedItem<Instance<T>, S> item, ManagerImpl manager)
   {
      return new InstanceBean<T, S>(item, manager);
   }

   /**
    * Constructor
    * 
    * @param field The underlying fields
    * @param manager The Web Beans manager
    */
   protected InstanceBean(AnnotatedItem<Instance<T>, S> field, ManagerImpl manager)
   {
      super(field, manager);
   }

   /**
    * Creates the implementing bean
    * 
    * @return The implementation
    */
   public Instance<T> create(CreationalContext<Instance<T>> creationalContext)
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         return new InstanceImpl<T>(getTypeParameter(), manager, getAnnotatedItem().getBindingsAsArray());
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }
   
   public void destroy(Instance<T> instance)
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         // TODO Implement any cleanup needed
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

}
