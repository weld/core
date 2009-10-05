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
package org.jboss.webbeans.resolution;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Reflections;

/**
 * @author pmuir
 *
 */
public class TypeSafeObserverResolver extends TypeSafeResolver<Resolvable, ObserverMethod<?,?>>
{

   private final BeanManagerImpl manager;

   public TypeSafeObserverResolver(BeanManagerImpl manager, Iterable<ObserverMethod<?,?>> observers)
   {
      super(observers);
      this.manager = manager;
   }

   @Override
   protected boolean matches(Resolvable resolvable, ObserverMethod<?,?> observer)
   {
      return Reflections.isAssignableFrom(observer.getObservedType(), resolvable.getTypeClosure()) && Beans.containsAllBindings(observer.getObservedQualifiers(), resolvable.getQualifiers(), manager);
   }
   
   /**
    * @return the manager
    */
   public BeanManagerImpl getManager()
   {
      return manager;
   }

   @Override
   protected Set<ObserverMethod<?,?>> filterResult(Set<ObserverMethod<?,?>> matched)
   {
      return matched;
   }

   @Override
   protected Iterable<ResolvableTransformer> getTransformers()
   {
      return Collections.emptySet();
   }

   @Override
   protected Set<ObserverMethod<?,?>> sortResult(Set<ObserverMethod<?,?>> matched)
   {
      return matched;
   }

}
