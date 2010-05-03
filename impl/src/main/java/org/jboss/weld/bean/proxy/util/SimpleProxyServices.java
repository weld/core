/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.serialization.spi.ProxyServices;

/**
 * A default implementation of the {@link ProxyServices} which simply use the
 * corresponding information from the proxy type. An exception is made for
 * {@code java.*} and {@code javax.*} packages which are often associated with
 * the system classloader and a more privileged ProtectionDomain.
 * 
 * @author David Allen
 */
public class SimpleProxyServices implements ProxyServices
{

   public ClassLoader getClassLoader(Class<?> type)
   {
      if (type.getName().startsWith("java"))
      {
         return this.getClass().getClassLoader();
      }
      else
      {
         return type.getClassLoader();
      }
   }

   public ProtectionDomain getProtectionDomain(Class<?> type)
   {
      if (type.getName().startsWith("java"))
      {
         return this.getClass().getProtectionDomain();
      }
      else
      {
         return type.getProtectionDomain();
      }
   }

   public void cleanup()
   {
      // This implementation requires no cleanup

   }

   public Object wrapForSerialization(Object proxyObject)
   {
      // Simply use our own replacement object for proxies
      return new SerializableProxy(proxyObject);
   }

   public Class<?> loadProxySuperClass(final String className)
   {
      try
      {
         return (Class<?>) AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               return Class.forName(className, true, cl);
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         throw new WeldException(BeanMessage.CANNOT_LOAD_CLASS, className, pae.getException());
      }
   }

}
