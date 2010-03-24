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

import java.lang.reflect.Constructor;
import static org.jboss.weld.logging.messages.ReflectionMessage.REFLECTIONFACTORY_INSTANTIATION_FAILED;
import java.lang.reflect.Method;

import org.jboss.weld.exceptions.WeldException;

/**
 * A instantiator for sun.reflect.ReflectionFactory
 * 
 * @author Nicklas Karlsson
 *
 */
public class ReflectionFactoryInstantiator implements Instantiator
{
   private static final String REFLECTION_CLASS_NAME = "sun.reflect.ReflectionFactory";

   private Method generator = null;
   private Object reflectionFactoryInstance = null;

   public ReflectionFactoryInstantiator()
   {
      try
      {
         Class<?> reflectionFactory = Class.forName(REFLECTION_CLASS_NAME);
         Method accessor = reflectionFactory.getMethod("getReflectionFactory");
         reflectionFactoryInstance = accessor.invoke(null);
         generator = reflectionFactory.getMethod("newConstructorForSerialization", new Class[] { Class.class, Constructor.class });
      }
      catch (Exception e)
      {
         // OK to fail
      }
   }

   @SuppressWarnings("unchecked")
   public <T> T instantiate(Class<T> clazz)
   {
      T instance = null;
      try
      {
         Constructor<T> instanceConstructor = (Constructor<T>) generator.invoke(reflectionFactoryInstance, new Object[] { clazz, Object.class.getDeclaredConstructor() });
         instance = instanceConstructor.newInstance();
      }
      catch (Exception e)
      {
         throw new WeldException(REFLECTIONFACTORY_INSTANTIATION_FAILED, e, clazz);
      }
      return instance;
   }

   public boolean isAvailable()
   {
      return generator != null && reflectionFactoryInstance != null;
   }

}
