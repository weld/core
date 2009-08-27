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
package org.jboss.webbeans.injection.spi.helpers;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.webbeans.injection.spi.JpaInjectionServices;

/**
 * An implementation of {@link JpaInjectionServices} which forwards all its method calls
 * to another {@link JpaInjectionServices}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link JpaInjectionServices} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingJpaInjectionServices implements JpaInjectionServices
{
   
   protected abstract JpaInjectionServices delegate();
   
   public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint)
   {
      return delegate().resolvePersistenceContext(injectionPoint);
   }
   
   public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint)
   {
      return delegate().resolvePersistenceUnit(injectionPoint);
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
}
