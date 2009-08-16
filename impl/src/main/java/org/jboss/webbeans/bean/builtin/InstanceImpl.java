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
package org.jboss.webbeans.bean.builtin;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.TypeLiteral;
import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.resolution.ResolvableWBClass;

/**
 * Helper implementation for Instance for getting instances
 * 
 * @author Gavin King
 * 
 * @param <T>
 */
public class InstanceImpl<T> extends AbstractFacade<T, Instance<T>> implements Instance<T>, Serializable
{

   private static final long serialVersionUID = -376721889693284887L;
   private static final Annotation[] EMPTY_BINDINGS = new Annotation[0];
   
   private final Set<Bean<?>> beans;

   public static <I> Instance<I> of(Type type, BeanManagerImpl manager, Set<Annotation> annotations)
   {
      return new InstanceImpl<I>(type, manager, annotations);
   }
   
   private InstanceImpl(Type type, BeanManagerImpl manager, Set<Annotation> bindings)
   {
      super(type, manager, bindings);
      this.beans = getManager().getBeans(getType(), bindings.toArray(EMPTY_BINDINGS));
   }

   public T get(Annotation... bindings)
   {
      Annotation[] annotations = mergeInBindings(bindings);
      Bean<T> bean = getManager().getBean(ResolvableWBClass.<T>of(getType(), annotations, getManager()), annotations);
      
      @SuppressWarnings("unchecked")
      T instance = (T) getManager().getReference(bean, getType(), getManager().createCreationalContext(bean));
      return instance;
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return "Obtainable instance for type " + getType() + " and binding types " + getBindings();
   }

   private Collection<T> getReferences()
   {
      Collection<T> instances = new ArrayList<T>();
      for (Bean<?> bean : beans)
      {
         Object object = getManager().getReference(bean, getType(), getManager().createCreationalContext(bean));
         
         @SuppressWarnings("unchecked")
         T instance = (T) object;
         
         instances.add(instance);
      }
      return instances;
   }
   
   public Iterator<T> iterator()
   {
      return getReferences().iterator();
   }

   public boolean isAmbiguous()
   {
      return beans.size() > 1;
   }

   public boolean isUnsatisfied()
   {
      return beans.size() == 0;
   }

   public Instance<T> select(Annotation... bindings)
   {
      return selectInstance(this.getType(), bindings);
   }

   public <U extends T> Instance<U> select(Class<U> subtype, Annotation... bindings)
   {
      return selectInstance(subtype, bindings);
   }

   public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... bindings)
   {
      return selectInstance(subtype.getType(), bindings);
   }
   
   private <U extends T> Instance<U> selectInstance(Type subtype, Annotation[] bindings)
   {
      return new InstanceImpl<U>(
            subtype, 
            this.getManager(), 
            new HashSet<Annotation>(Arrays.asList(mergeInBindings(bindings))));
   } 

}
