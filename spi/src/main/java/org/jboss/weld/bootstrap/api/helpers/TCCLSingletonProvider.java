/*
 *  JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
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

package org.jboss.weld.bootstrap.api.helpers;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Map;

import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;

/**
 * Singleton provider that uses the Thread Context ClassLoader to differentiate
 * between applications
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Pete Muir
 */
public class TCCLSingletonProvider extends SingletonProvider
{
   
   @Override
   public <T> Singleton<T> create(Class<? extends T> type)
   {
      return new TCCLSingleton<T>();
   }

   private static class TCCLSingleton<T> implements Singleton<T>
   {
      // use Hashtable for concurrent access
      private final Map<ClassLoader, T> store = new Hashtable<ClassLoader, T>();

      public T get()
      {
         T instance = store.get(getClassLoader());
         if (instance == null)
         {
            throw new IllegalStateException("Singleton not set for " + getClassLoader());
         }
         return instance;
      }

      public void set(T object)
      {
         // TODO remove this
         System.out.println("Adding singleton for " + getClassLoader());
         store.put(getClassLoader(), object);
      }
      
      public void clear()
      {
         store.remove(getClassLoader());
      }
      
      public boolean isSet()
      {
         return store.containsKey(getClassLoader());
      }

      private ClassLoader getClassLoader()
      {
         SecurityManager sm = System.getSecurityManager();
         if (sm != null)
         {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
            {
               public ClassLoader run()
               {
                  return Thread.currentThread().getContextClassLoader();
               }
            });
         }
         else
         {
            return Thread.currentThread().getContextClassLoader();
         }
      }
   }
}
