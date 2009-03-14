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

import java.util.Iterator;

import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * More powerful version of {@link EjbDescriptor} that exposes Maps for some
 * metadata. The {@link EjbDescriptor} to enhance should be passed to the
 * constructor
 * 
 * @author Pete Muir
 * 
 */
public class InternalEjbDescriptor<T> extends ForwardingEjbDescriptor<T> implements EjbDescriptor<T>
{
   
   private final Class<?> objectInterface;
   private final boolean local; 
   private final EjbDescriptor<T> delegate;
   
   public InternalEjbDescriptor(EjbDescriptor<T> ejbDescriptor)
   {
      this.delegate = ejbDescriptor;
      Iterator<BusinessInterfaceDescriptor<?>> it = ejbDescriptor.getLocalBusinessInterfaces().iterator();
      if (it.hasNext())
      {
         this.objectInterface = it.next().getInterface();
         this.local = true;
      }
      else
      {
         this.objectInterface = null;
         this.local = false;
      }
   }
   
   @Override
   protected EjbDescriptor<T> delegate()
   {
      return delegate;
   }
   
   public boolean isLocal()
   {
      return local;
   }
   
   public Class<?> getObjectInterface()
   {
      return objectInterface;
   }
   
}
