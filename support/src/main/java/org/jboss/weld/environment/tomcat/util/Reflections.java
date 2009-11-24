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
package org.jboss.weld.environment.tomcat.util;


/**
 * @author pmuir
 *
 */
public class Reflections
{
   
   
   public static <T> T newInstance(String className)
   {
      try
      {
         return Reflections.<T>classForName(className).newInstance();
      }
      catch (InstantiationException e)
      {
         throw new IllegalArgumentException("Cannot instantiate instance of " + className + " with no-argument constructor", e);
      }
      catch (IllegalAccessException e)
      {
         throw new IllegalArgumentException("Cannot instantiate instance of " + className + " with no-argument constructor", e);
      }
   }
   
   
   public static <T> Class<T> classForName(String name)
   {
      
      try
      {
         if (Thread.currentThread().getContextClassLoader() != null)
         {
            Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(name);
            
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>)  c;
            
            return clazz;
         }
         else
         {
            Class<?> c = Class.forName(name);
            
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>)  c;
            
            return clazz;
         }
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException("Cannot load class for " + name, e);
      }
      catch (NoClassDefFoundError e)
      {
         throw new IllegalArgumentException("Cannot load class for " + name, e);
      }
   }

}
