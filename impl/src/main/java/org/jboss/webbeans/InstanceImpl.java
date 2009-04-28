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
package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.inject.Instance;

import org.jboss.webbeans.injection.resolution.ResolvableAnnotatedClass;

/**
 * Helper implementation for Instance for getting instances
 * 
 * @author Gavin King
 * 
 * @param <T>
 * @see javax.webbeans.Instace
 */
public class InstanceImpl<T> extends FacadeImpl<T> implements Instance<T>
{
   

   public static <I> Instance<I> of(Type type, ManagerImpl manager, Set<Annotation> annotations)
   {
      return new InstanceImpl<I>(type, manager, annotations);
   }
   
   /**
    * Constructor
    * 
    * @param type The obtainable type
    * @param manager The Web Beans manager
    * @param bindings The binding types
    */
   private InstanceImpl(Type type, ManagerImpl manager, Set<Annotation> bindings)
   {
      super(type, manager, bindings);
   }

   /**
    * Gets an instance with the matching binding types
    * 
    * @param bindings The binding types
    * @return The instance
    * 
    * @see javax.inject.Instance#get(Annotation...)
    * @see javax.inject.manager.Manager#getInstanceByType(Class, Annotation...)
    */
   public T get(Annotation... bindings) 
   {
      Annotation[] annotations = mergeInBindings(bindings);
      return getManager().getInstanceByType(ResolvableAnnotatedClass.<T>of(type, annotations), annotations);
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return "Obtainable instance for type " + type + " and binding types " + bindings;
   }

}
