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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.ExecutionException;

/**
 * Utility class for static reflection-type operations
 * 
 * @author Pete Muir
 * 
 */
public class Reflections
{

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
    * Checks if class is a static inner one
    * 
    * @param clazz Class to check
    * @return True if static, false otherwise
    */
   public static boolean isStaticInnerClass(Class<?> clazz)
   {
      return clazz.isMemberClass() && isStatic(clazz);
   }

   /**
    * Checks if class is a non-static inner one
    * 
    * @param clazz Class to Check
    * @return True if static, false otherwise
    */
   public static boolean isNonStaticInnerClass(Class<?> clazz)
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
         return clazz.getConstructor(parameterTypes);
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
    * Gets all methods with a given annotation
    * 
    * @param clazz The class the examine
    * @param annotationType The annotation type to search for
    * @return A list of matching methods. An empty list is returned if no
    *         matches are found
    */
   public static List<Method> getMethods(Class<?> clazz, Class<? extends Annotation> annotationType)
   {
      List<Method> methods = new ArrayList<Method>();
      for (Method method : clazz.getMethods())
      {
         if (method.isAnnotationPresent(annotationType))
         {
            methods.add(method);
         }
      }
      return methods;
   }

   /**
    * Gets all constructors with a given annotation
    * 
    * @param <T> The type of the class
    * @param clazz The class
    * @param annotationType The annotation type
    * @return A list of matching constructors. An empty list is returned if no
    *         matches are found
    */
   @SuppressWarnings("unchecked")
   public static <T> List<Constructor<T>> getAnnotatedConstructors(Class<? extends T> clazz, Class<? extends Annotation> annotationType)
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<?> constructor : clazz.getConstructors())
      {
         if (constructor.isAnnotationPresent(annotationType))
         {
            constructors.add((Constructor<T>) constructor);
         }
      }
      return constructors;
   }

   /**
    * Gets constructors with a given annotated parameter
    * 
    * @param <T> The type
    * @param clazz The class
    * @param parameterAnnotationType The parameter annotation type
    * @return A list of matching constructors. An empty list is returned if no
    *         matches are found
    */
   @SuppressWarnings("unchecked")
   public static <T> List<Constructor<T>> getConstructorsForAnnotatedParameter(Class<? extends T> clazz, Class<? extends Annotation> parameterAnnotationType)
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<?> constructor : clazz.getConstructors())
      {
         for (Annotation[] annotations : constructor.getParameterAnnotations())
         {
            for (Annotation annotation : annotations)
            {
               if (annotation.annotationType().equals(parameterAnnotationType))
               {
                  constructors.add((Constructor<T>) constructor);
               }
            }
         }
      }
      return constructors;
   }

   /**
    * Gets constructors with a given meta-annotated parameter
    * 
    * @param <T> The type
    * @param clazz The class
    * @param metaAnnotationType The parameter meta-annotation type
    * @return A list of matching constructors. An empty list is returned if no
    *         matches are found
    */
   @SuppressWarnings("unchecked")
   public static <T> List<Constructor<T>> getConstructorsForMetaAnnotatedParameter(Class<? extends T> clazz, Class<? extends Annotation> metaAnnotationType)
   {
      List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
      for (Constructor<?> constructor : clazz.getConstructors())
      {
         for (Annotation[] annotations : constructor.getParameterAnnotations())
         {
            for (Annotation annotation : annotations)
            {
               if (annotation.annotationType().isAnnotationPresent(metaAnnotationType))
               {
                  constructors.add((Constructor<T>) constructor);
               }
            }
         }
      }
      return constructors;
   }

   /**
    * Checks if all annotations types are in a given set of annotations
    * 
    * @param annotations The annotation set
    * @param annotationTypes The annotation types to match
    * @return True if match, false otherwise
    */
   public static boolean annotationTypeSetMatches(Set<Class<? extends Annotation>> annotations, Class<? extends Annotation>... annotationTypes)
   {
      List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
      annotationTypeList.addAll(Arrays.asList(annotationTypes));
      for (Class<? extends Annotation> annotation : annotations)
      {
         if (annotationTypeList.contains(annotation))
         {
            annotationTypeList.remove(annotation);
         }
         else
         {
            return false;
         }
      }
      return annotationTypeList.size() == 0;
   }

   /**
    * Checks if all annotations are in a given set of annotations
    * 
    * @param annotations The annotation set
    * @param annotationTypes The annotations to match
    * @return True if match, false otherwise
    */
   public static boolean annotationSetMatches(Set<Annotation> annotations, Class<? extends Annotation>... annotationTypes)
   {
      List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
      annotationTypeList.addAll(Arrays.asList(annotationTypes));
      for (Annotation annotation : annotations)
      {
         if (annotationTypeList.contains(annotation.annotationType()))
         {
            annotationTypeList.remove(annotation.annotationType());
         }
         else
         {
            return false;
         }
      }
      return annotationTypeList.size() == 0;
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

   /**
    * Invokes a method and wraps exceptions
    * 
    * @param methodName The method name to find on the instance and invoke
    * @param parameterTypes The method name to find on the instance and invoke
    * @param instance The instance to invoke on
    * @param parameterValues The parameters values
    * @return The return value
    */
   public static Object invokeAndWrap(String methodName, Class<?>[] parameterTypes, Object instance, Object[] parameterValues)
   {
      try
      {
         return instance.getClass().getMethod(methodName, parameterTypes).invoke(instance, parameterValues);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error invoking method " + methodName + " on " + instance.getClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error invoking method " + methodName + " on " + instance.getClass(), e);
      }
      catch (InvocationTargetException e)
      {
         throw new ExecutionException("Error invoking method " + methodName + " on " + instance.getClass(), e);
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
    * Sets value of a field and wraps exceptions
    * 
    * @param field The field to set on
    * @param target The instance to set on
    * @param value The value to set
    */
   public static void setAndWrap(Field field, Object target, Object value)
   {
      try
      {
         field.set(target, value);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error setting field " + field.getName() + " on " + field.getDeclaringClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error setting field " + field.getName() + " on " + field.getDeclaringClass(), e);
      }
   }

   /**
    * Sets value of a field and wraps exceptions
    * 
    * @param field The field to set on
    * @param target The instance to set on
    * @param value The value to set
    */
   public static void setAndWrap(String fieldName, Object target, Object value)
   {
      try
      {
         target.getClass().getField(fieldName).set(target, value);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error setting field " + fieldName + " on " + target.getClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error setting field " + fieldName + " on " + target.getClass(), e);
      }
      catch (SecurityException e)
      {
         throw new ExecutionException("Error setting field " + fieldName + " on " + target.getClass(), e);
      }
      catch (NoSuchFieldException e)
      {
         throw new ExecutionException("Error setting field " + fieldName + " on " + target.getClass(), e);
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

   /**
    * Looks up a method in the type hierarchy of an instance
    * 
    * @param method The method to look for
    * @param instance The instance to start from
    * @return The method found, or an NoSuchMethodException if it is not found
    */
   public static Method lookupMethod(Method method, Object instance)
   {
      for (Class<? extends Object> clazz = instance.getClass(); clazz != Object.class; clazz = clazz.getSuperclass())
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
    * @param clazz The class to examine
    * @return The set of classes and interfaces in the hierarchy
    * @see #getTypeHierachy(Class, Set)
    */
   public static Set<Class<?>> getTypeHierachy(Class<?> clazz)
   {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      getTypeHierachy(clazz, classes);
      return classes;
   }
   
   /**
    * Gets the flattened type hierarchy for a class, including all super classes
    * and the entire interface type hierarchy
    * 
    * @param clazz the class to examine
    * @param classes the set of types
    */
   public static void getTypeHierachy(Class<?> clazz, Set<? super Class<?>> classes)
   {
      if (clazz != null)
      {
         classes.add(clazz);
         getTypeHierachy(clazz.getSuperclass(), classes);
         for (Class<?> c : clazz.getInterfaces())
         {
            getTypeHierachy(c, classes);
         }
      }
   }

   /**
    * Checks the bindingType to make sure the annotation was declared properly
    * as a binding type (annotated with @BindingType).
    * 
    * @param bindingType The binding type to check
    * @return true only if the annotation is really a binding type
    */
   public static boolean isBindingType(Annotation bindingType)
   {
      boolean isBindingAnnotation = false;
      if (bindingType.annotationType().isAnnotationPresent(BindingType.class))
      {
         isBindingAnnotation = true;
      }
      return isBindingAnnotation;
   }

   public static boolean isSerializable(Class<?> clazz)
   {
      return getTypeHierachy(clazz).contains(Serializable.class);
   }
}
