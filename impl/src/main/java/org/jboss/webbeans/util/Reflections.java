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

package org.jboss.webbeans.util;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.inject.BindingType;
import javax.inject.ExecutionException;


/**
 * Utility class for static reflection-type operations
 * 
 * @author Pete Muir
 * 
 */
public class Reflections
{
   
   public static class HierarchyDiscovery<T extends Type>
   {
      
      private final T type;
     
      private Set<T> types;
      
      public HierarchyDiscovery(T type)
      {
         this.type = type;
      }
      
      protected void add(T type)
      {
         types.add(type);
      }
      
      public Set<T> getFlattenedTypes()
      {
         if (types == null)
         {
            this.types = new HashSet<T>();
            discoverTypes(type);
         }
         return types;
      }
      
      
      private void discoverTypes(T type)
      {
         if (type != null)
         {
            add(type);
            if (type instanceof Class)
            {
               discoverFromClass((Class<?>) type);
            }
            else if (type instanceof ParameterizedType)
            {
               Type rawType = ((ParameterizedType) type).getRawType();
               if (rawType instanceof Class)
               {
                  discoverFromClass((Class<?>) rawType);
               }
            }
         }
      }
      
      @SuppressWarnings("unchecked")
      private void discoverFromClass(Class<?> clazz)
      {
         discoverTypes((T) clazz.getSuperclass());
         for (Class<?> c : clazz.getInterfaces())
         {
            discoverTypes((T) c);
         }
      }
      
   }

   /**
    * Gets the property name from a getter method
    * 
    * @param method The getter method
    * @return The name of the property. Returns null if method wasn't JavaBean
    *         getter-styled
    */
   public static String getPropertyName(Method method)
   {
      String methodName = method.getName();
      if (methodName.matches("^(get).*") && method.getParameterTypes().length == 0)
      {
         return Introspector.decapitalize(methodName.substring(3));
      }
      else if (methodName.matches("^(is).*") && method.getParameterTypes().length == 0)
      {
         return Introspector.decapitalize(methodName.substring(2));
      }
      else
      {
         return null;
      }

   }

   /**
    * Checks if class is final
    * 
    * @param clazz The class to check
    * @return True if final, false otherwise
    */
   public static boolean isFinal(Class<?> clazz)
   {
      return Modifier.isFinal(clazz.getModifiers());
   }

   /**
    * Checks if member is final
    * 
    * @param member The member to check
    * @return True if final, false otherwise
    */
   public static boolean isFinal(Member member)
   {
      return Modifier.isFinal(member.getModifiers());
   }

   /**
    * Checks if type or member is final
    * 
    * @param type Type or member
    * @return True if final, false otherwise
    */
   public static boolean isTypeOrAnyMethodFinal(Class<?> type)
   {
      if (isFinal(type))
      {
         return true;
      }
      for (Method method : type.getDeclaredMethods())
      {
         if (isFinal(method))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Checks if type is primitive
    * 
    * @param type Type to check
    * @return True if primitive, false otherwise
    */
   public static boolean isPrimitive(Class<?> type)
   {
      return type.isPrimitive();
   }

   /**
    * Checks if type is static
    * 
    * @param type Type to check
    * @return True if static, false otherwise
    */
   public static boolean isStatic(Class<?> type)
   {
      return Modifier.isStatic(type.getModifiers());
   }

   /**
    * Checks if member is static
    * 
    * @param member Member to check
    * @return True if static, false otherwise
    */
   public static boolean isStatic(Member member)
   {
      return Modifier.isStatic(member.getModifiers());
   }

   public static boolean isTransient(Member member)
   {
      return Modifier.isTransient(member.getModifiers());
   }

   /**
    * Checks if clazz is abstract
    * 
    * @param clazz Class to check
    * @return True if abstract, false otherwise
    */
   public static boolean isAbstract(Class<?> clazz)
   {
      return Modifier.isAbstract(clazz.getModifiers());
   }

   /**
    * Checks if class is a non-static inner one
    * 
    * @param clazz Class to Check
    * @return True if static, false otherwise
    */
   public static boolean isNonMemberInnerClass(Class<?> clazz)
   {
      return clazz.isMemberClass() && !isStatic(clazz);
   }

   /**
    * Gets a constructor with matching parameter types
    * 
    * @param <T> The type
    * @param clazz The class
    * @param parameterTypes The parameter types
    * @return The matching constructor. Null is returned if none is found
    */
   public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes)
   {
      try
      {
         return clazz.getDeclaredConstructor(parameterTypes);
      }
      catch (NoSuchMethodException e)
      {
         return null;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error accessing constructor (with parameters " + parameterTypes + ") of " + clazz, e);
      }
   }

   /**
    * Gets the actual type arguments of a class
    * 
    * @param clazz The class to examine
    * @return The type arguments
    */
   public static Type[] getActualTypeArguments(Class<?> clazz)
   {
      if (clazz.getGenericSuperclass() instanceof ParameterizedType)
      {
         return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
      }
      else
      {
         return new Type[0];
      }
   }

   /**
    * Checks if raw type is array type
    * 
    * @param rawType The raw type to check
    * @return True if array, false otherwise
    */
   public static boolean isArrayType(Class<?> rawType)
   {
      return rawType.isArray();
   }

   /**
    * Checks if type is parameterized type
    * 
    * @param type The type to check
    * @return True if parameterized, false otherwise
    */
   public static boolean isParameterizedType(Class<?> type)
   {
      return type.getTypeParameters().length > 0;
   }

   /**
    * Invokes a method and wraps exceptions
    * 
    * @param method The method to invoke
    * @param instance The instance to invoke on
    * @param parameters The parameters
    * @return The return value
    */
   public static Object invokeAndWrap(Method method, Object instance, Object... parameters)
   {
      try
      {
         method.setAccessible(true);
         return method.invoke(instance, parameters);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error invoking method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error invoking method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (InvocationTargetException e)
      {
         throw new ExecutionException("Error invoking method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
   }
   
   public static Object invokeAndWrap(String methodName, Object instance, Object... parameters)
   {
      Class<?>[] parameterTypes = new Class<?>[parameters.length];
      for (int i = 0; i < parameters.length; i++)
      {
         parameterTypes[i] = parameters[i].getClass();
      }
      try
      {
         return invokeAndWrap(instance.getClass().getMethod(methodName, parameterTypes), instance, parameters);
      }
      catch (SecurityException e)
      {
         throw new ExecutionException("Error invoking method " + methodName + " on " + instance.getClass(), e);
      }
      catch (NoSuchMethodException e)
      {
         throw new ExecutionException("Error invoking method " + methodName + " on " + instance.getClass(), e);
      }
   }

   /**
    * Gets value of a field and wraps exceptions
    * 
    * @param field The field to set on
    * @param target The instance to set on
    * @return The value to set
    */
   public static Object getAndWrap(Field field, Object target)
   {
      try
      {
         return field.get(target);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error getting field " + field.getName() + " on " + field.getDeclaringClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error getting field " + field.getName() + " on " + field.getDeclaringClass(), e);
      }
   }
   
   public static Object getAndWrap(String fieldName, Object target)
   {
      try
      {
         return getAndWrap(target.getClass().getField(fieldName), target);
      }
      catch (SecurityException e)
      {
         throw new ExecutionException("Error getting field " + fieldName + " on " + target.getClass(), e);
      }
      catch (NoSuchFieldException e)
      {
         throw new ExecutionException("Error getting field " + fieldName + " on " + target.getClass(), e);
      }
   }

   /**
    * Looks up a method in the type hierarchy of an instance
    * 
    * @param method The method to look for
    * @param instance The instance to start from
    * @return The method found, or an NoSuchMethodException if it is not found
    */
   public static Method lookupMethod(Method method, Object instance)
   {
      for (Class<? extends Object> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass())
      {
         try
         {
            Method targetMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (!targetMethod.isAccessible())
            {
               targetMethod.setAccessible(true);
            }
            return targetMethod;
         }
         catch (NoSuchMethodException nsme)
         {
            // Expected, nothing to see here.
         }
      }
      throw new IllegalArgumentException("Method " + method.getName() + " not implemented by instance");
   }

   /**
    * Indicates if an instance is a Javassist proxy
    * 
    * @param instance The instance to examine
    * @return True if proxy, false otherwise
    */
   public static boolean isProxy(Object instance)
   {
      return instance.getClass().getName().indexOf("_$$_javassist_") > 0;
   }

   /**
    * Gets the type hierarchy for a class
    * 
    * A recursive function that adds the class to the set of type and then calls
    * itself with the suprerclass as paramater until the top of the hierarchy is
    * reached. For each steps, adds all interfaces of the class to the set.
    * Since the data structure is a set, duplications are eliminated
    * 
    * 
    * @deprecated see {@link HierarchyDiscovery} 
    * @param clazz The class to examine
    * @return The set of classes and interfaces in the hierarchy
    * @see #getTypeHierachy(Class, Set)
    */
   @Deprecated
   public static Set<Class<?>> getTypeHierachy(Class<?> clazz)
   {
      return new HierarchyDiscovery<Class<?>>(clazz).getFlattenedTypes();
   }

   /**
    * Checks the bindingType to make sure the annotation was declared properly
    * as a binding type (annotated with @BindingType).
    * 
    * @param binding The binding type to check
    * @return true only if the annotation is really a binding type
    */
   @Deprecated
   // TODO Replace usage of this with metadatacache
   public static boolean isBindings(Annotation binding)
   {
      boolean isBindingAnnotation = false;
      if (binding.annotationType().isAnnotationPresent(BindingType.class))
      {
         isBindingAnnotation = true;
      }
      return isBindingAnnotation;
   }

   public static boolean isSerializable(Class<?> clazz)
   {
      return getTypeHierachy(clazz).contains(Serializable.class) || clazz.isPrimitive();
   }

}
