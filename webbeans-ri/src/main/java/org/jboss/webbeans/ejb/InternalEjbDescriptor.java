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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

   private final Map<Class<?>, String> localBusinessInterfacesJndiNames;
   private final EjbDescriptor<T> delegate;
   public List<Method> removeMethods;

   public InternalEjbDescriptor(EjbDescriptor<T> ejbDescriptor)
   {
      this.delegate = ejbDescriptor;
      this.localBusinessInterfacesJndiNames = new HashMap<Class<?>, String>();
      for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces())
      {
         localBusinessInterfacesJndiNames.put(businessInterfaceDescriptor.getInterface(), businessInterfaceDescriptor.getJndiName());
      }
      // Internally, Object.class is added to the type hierachy of an
      // EnterpriseBean, so we need to represent that here. We can just use any
      // of the local business interfaces
      localBusinessInterfacesJndiNames.put(Object.class, ejbDescriptor.getLocalBusinessInterfaces().iterator().next().getJndiName());
      removeMethods = new ArrayList<Method>();
      for (Method removeMethod : ejbDescriptor.getRemoveMethods())
      {
         removeMethods.add(removeMethod);
      }
   }

   public Map<Class<?>, String> getLocalBusinessInterfacesJndiNames()
   {
      return Collections.unmodifiableMap(localBusinessInterfacesJndiNames);
   }

   public List<Method> getRemoveMethods()
   {
      return Collections.unmodifiableList(removeMethods);
   }

   @Override
   protected EjbDescriptor<T> delegate()
   {
      return delegate;
   }

}
