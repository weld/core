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

package org.jboss.webbeans.bootstrap.api;

import org.jboss.webbeans.bootstrap.api.helpers.IsolatedStaticSingletonProvider;
import org.jboss.webbeans.bootstrap.api.helpers.TCCLSingletonProvider;

/**
 * A provider of {@link Singleton}s
 * 
 * @see IsolatedStaticSingletonProvider
 * @see TCCLSingletonProvider
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Pete Muir
 */
public abstract class SingletonProvider
{
   /*
    * Singleton pattern. Upon first access (see instance()), it initializes
    * itself by a default implementation. Containers are free to explicitly
    * initialize it by calling initialize() method.
    */
   private static volatile SingletonProvider INSTANCE;

   private static final String DEFAULT_SCOPE_FACTORY = IsolatedStaticSingletonProvider.class.getName();

   public static SingletonProvider instance()
   {
      if (INSTANCE == null)
      {
         synchronized (SingletonProvider.class)
         {
            if (INSTANCE == null)
            {
               /*
                * TODO: We should discover ScopeFactory implementation using
                * Service Provider Mechanism. In the absence of any explicitly
                * configured service, should we default to the default
                * implementation.
                */
               initializeWithDefaultScope();
            }
         }
      }
      return INSTANCE;
   }

   protected SingletonProvider()
   {
   }

   /**
    * Create a new singleton
    * 
    * @param expectedType represents the type of Java object stored in the singleton
    * @return a singelton
    */
   public abstract <T> Singleton<T> create(Class<? extends T> expectedType);

   /**
    * Initialize with the default instance
    */
   private static void initializeWithDefaultScope()
   {
      try
      {
         Class<?> aClass = Class.forName(DEFAULT_SCOPE_FACTORY);
         INSTANCE = (SingletonProvider) aClass.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Initialize with an explicit instance
    * 
    * @param instance
    */
   public static void initialize(SingletonProvider instance)
   {
      synchronized (SingletonProvider.class)
      {
         if (INSTANCE == null)
         {
            INSTANCE = instance;
         }
         else
         {
            throw new RuntimeException("ScopeFactory is already initialized with " + INSTANCE);
         }
      }
   }

}
