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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.context.Dependent;
import javax.inject.manager.Bean;

import org.jboss.webbeans.RootManager;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;

/**
 * Abstract base class with functions specific to RI built-in beans
 *  
 * @author Pete Muir
 */
public abstract class RIBean<T> extends Bean<T>
{
   
   private static final ConcurrentMap<String, AtomicInteger> ids = new ConcurrentHashMap<String, AtomicInteger>();
   
   private final RootManager manager;

   protected RIBean(RootManager manager)
   {
      super(manager);
      this.manager = manager;
      // TODO better ID strategy (human readable)
   }
   
   protected static String createId(String prefix)
   {
      AtomicInteger i = ids.putIfAbsent(prefix, new AtomicInteger());
      return prefix + "-" + i;
   }

   @Override
   protected RootManager getManager()
   {
      return manager;
   }

   public abstract Class<T> getType();
   
   public abstract void initialize(BeanDeployerEnvironment environment);

   public abstract boolean isSpecializing();

   public boolean isDependent()
   {
      return getScopeType().equals(Dependent.class);
   }

   public abstract boolean isProxyable();

   public abstract boolean isPrimitive();

   public abstract Set<AnnotatedInjectionPoint<?, ?>> getInjectionPoints();

   public abstract RIBean<?> getSpecializedBean();
   
   public abstract String getId();
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof RIBean)
      {
         RIBean<?> that = (RIBean<?>) obj;
         return this.getId().equals(that.getId());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getId().hashCode();
   }

}
