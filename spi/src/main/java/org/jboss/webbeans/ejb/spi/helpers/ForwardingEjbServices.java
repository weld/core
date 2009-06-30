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
package org.jboss.webbeans.ejb.spi.helpers;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;

/**
 * An implementation of {@link EjbServices} which forwards all its method calls
 * to another {@link EjbServices}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link EjbServices} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingEjbServices implements EjbServices
{
   
   public abstract EjbServices delegate();
   
   public Object resolveEjb(InjectionPoint injectionPoint)
   {
      return delegate().resolveEjb(injectionPoint);
   }
   
   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor)
   {
      return delegate().resolveEjb(ejbDescriptor);
   }
   
   public Object resolveRemoteEjb(String jndiName, String mappedName, String ejbLink)
   {
      return delegate().resolveRemoteEjb(jndiName, mappedName, ejbLink);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
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
   
}
