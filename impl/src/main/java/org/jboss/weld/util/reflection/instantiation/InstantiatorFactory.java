/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.util.reflection.instantiation;

import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.bootstrap.api.Service;

/**
 * A factory class for obtaining the first available instantiator
 * 
 * @author Nicklas Karlsson
 * 
 */
@SuppressWarnings("serial")
public class InstantiatorFactory implements Service
{
   private static Instantiator availableInstantiator;

   private static final List<Instantiator> instantiators = new ArrayList<Instantiator>()
   {
      {
         add(new UnsafeInstantiator());
         add(new ReflectionFactoryInstantiator());
      }
   };

   static
   {
      for (Instantiator instantiator : instantiators)
      {
         if (instantiator.isAvailable())
         {
            availableInstantiator = instantiator;
            break;
         }
      }
   }

   public static Instantiator getInstantiator()
   {
      return availableInstantiator;
   }
   
   public static boolean useInstantiators() 
   {
      return "true".equals(System.getProperty("org.jboss.weld.instantiators"));
   }

   public void cleanup()
   {
      instantiators.clear();
   }
}
