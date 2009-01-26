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

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;

/**
 * Bean for InjectionPoint metadata
 * 
 * @author David Allen
 * 
 */
public class InjectionPointBean<T, S> extends AbstractFacadeBean<T, S, Object>
{

   /**
    * Creates an InjectionPoint Web Bean for the injection of the containing bean owning
    * the field, constructor or method for the annotated item
    * 
    * @param <T> must be InjectionPoint
    * @param <S>
    * @param field The annotated member field/parameter for the injection
    * @param manager The RI manager implementation
    * @return a new bean for this injection point
    */
   public static <T, S> InjectionPointBean<T, S> of(AnnotatedItem<T, S> field, ManagerImpl manager)
   {
      return new InjectionPointBean<T, S>(field, manager);
   }

   protected InjectionPointBean(AnnotatedItem<T, S> field, ManagerImpl manager)
   {
      super(field, manager);
   }

   public T create(CreationalContext<T> creationalContext)
   {
      // TODO Why do we need to cast here?
      return getType().cast(manager.getInjectionPointProvider().getPreviousInjectionPoint());
   }

   public void destroy(T instance)
   {
      // The instance is always in the Dependent context and can be garbage
      // collected
   }

}
