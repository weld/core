/*
 * JBoss, Home of Professional Open Source
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

package org.jboss.webbeans.bootstrap.api.helpers;

import org.jboss.webbeans.bootstrap.api.Singleton;
import org.jboss.webbeans.bootstrap.api.SingletonProvider;

/**
 * 
 * A singleton provider that assumes an isolated classloder per application
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Pete Muir
 */
public class IsolatedStaticSingletonProvider extends SingletonProvider
{

   @Override
   public <T> Singleton<T> create(Class<? extends T> type)
   {
      return new IsolatedStaticSingleton<T>();
   }

   private static class IsolatedStaticSingleton<T> implements Singleton<T>
   {
      private T object;

      public T get()
      {
         if (object == null)
         {
            throw new IllegalStateException("Singleton is not set");
         }
         return object;
      }

      public void set(T object)
      {
         this.object = object;
      }
      
      public void clear()
      {
         this.object = null;
      }
      
      public boolean isSet()
      {
         return object != null;
      }
   }
}
