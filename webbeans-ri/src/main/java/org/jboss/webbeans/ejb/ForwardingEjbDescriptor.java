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
package org.jboss.webbeans.ejb;

import java.lang.reflect.Method;

import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * Forwarding helper class for {@link EjbDescriptor} to support the decorator
 * pattern
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingEjbDescriptor<T> implements EjbDescriptor<T>
{
   
   protected abstract EjbDescriptor<T> delegate();
   
   public String getEjbName()
   {
      return delegate().getEjbName();
   }
   
   public Iterable<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces()
   {
      return delegate().getLocalBusinessInterfaces();
   }
   
   public Iterable<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces()
   {
      return delegate().getRemoteBusinessInterfaces();
   }
   
   public Iterable<Method> getRemoveMethods()
   {
      return delegate().getRemoveMethods();
   }
   
   public Class<T> getType()
   {
      return delegate().getType();
   }
   
   public boolean isMessageDriven()
   {
      return delegate().isMessageDriven();
   }
   
   public boolean isSingleton()
   {
      return delegate().isSingleton();
   }
   
   public boolean isStateful()
   {
      return delegate().isStateful();
   }
   
   public boolean isStateless()
   {
      return delegate().isStateless();
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
