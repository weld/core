/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.reflection;

import static org.jboss.weld.logging.Category.UTIL;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Qualifier;

import org.jboss.weld.util.Types;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;

import ch.qos.cal10n.IMessageConveyor;

/**
 * Utility class for static reflection-type operations
 * 
 * @author Pete Muir
 * 
 */
public class Reflections
{

   static final LocLogger log = loggerFactory().getLogger(UTIL);
   static final XLogger xLog = loggerFactory().getXLogger(UTIL);

   // Exception messages
   private static final IMessageConveyor messageConveyer = loggerFactory().getMessageConveyor();

   public static final Type[] EMPTY_TYPES = {};
   public static final Annotation[] EMPTY_ANNOTATIONS = {};
   public static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

   public static Map<Class<?>, Type> buildTypeMap(Set<Type> types)
   {
      Map<Class<?>, Type> map = new HashMap<Class<?>, Type>();
      for (Type type : types)
      {
         if (type instanceof Class<?>)
         {
            map.put((Class<?>) type, type);
         }
         else if (type instanceof ParameterizedType)
         {
            if (((ParameterizedType) type).getRawType() instanceof Class<?>)
            {
               map.put((Class<?>) ((ParameterizedType) type).getRawType(), type);
            }
         }
         else if (type instanceof TypeVariable<?>)
         {
            
         }
      }
      return map;
   }

   /**
    * Gets the property name from a getter method.
    * 
    * We extend JavaBean conventions, allowing the getter method to have parameters
    * 
    * @param method The getter method
    * @return The name of the property. Returns null if method wasn't JavaBean
    *         getter-styled
    */
   public static String getPropertyName(Method method)
   {
      String methodName = method.getName();
      if (methodName.matches("^(get).*"))
      {
         return Introspector.decapitalize(methodName.substring(3));
      }
      else if (methodName.matches("^(is).*"))
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

   public static int getNesting(Class<?> clazz)
   {
      if (clazz.isMemberClass() && !isStatic(clazz))
      {
         return 1 + getNesting(clazz.getDeclaringClass());
      }
      else
      {
         return 0;
      }
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

   public static boolean isPackagePrivate(int mod)
   {
      return !(Modifier.isPrivate(mod) || Modifier.isProtected(mod) || Modifier.isPublic(mod));
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
    * Checks if a method is abstract
    * 
    * @param method
    * @return
    */
   public static boolean isAbstract(Method method)
   {
      return Modifier.isAbstract(method.getModifiers());
   }

   /**
    * Gets the actual type arguments of a class
    * 
    * @param clazz The class to examine
    * @return The type arguments
    */
   public static Type[] getActualTypeArguments(Class<?> clazz)
   {
      Type type = new HierarchyDiscovery(clazz).getResolvedType();
      if (type instanceof ParameterizedType)
      {
         return ((ParameterizedType) type).getActualTypeArguments();
      }
      else
      {
         return EMPTY_TYPES;
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

   public static boolean isParamerterizedTypeWithWildcard(Class<?> type)
   {
      if (isParameterizedType(type))
      {
         return containsWildcards(type.getTypeParameters());
      }
      else
      {
         return false;
      }
   }

   public static boolean containsWildcards(Type[] types)
   {
      for (Type type : types)
      {
         if (type instanceof WildcardType)
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Checks the bindingType to make sure the annotation was declared properly
    * as a binding type (annotated with @BindingType) and that it has a runtime
    * retention policy.
    * 
    * @param binding The binding type to check
    * @return true only if the annotation is really a binding type
    */
   @Deprecated
   // TODO Replace usage of this with metadatacache
   public static boolean isBindings(Annotation binding)
   {
      boolean isBindingAnnotation = false;
      if (binding.annotationType().isAnnotationPresent(Qualifier.class) && binding.annotationType().isAnnotationPresent(Retention.class) && binding.annotationType().getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME))
      {
         isBindingAnnotation = true;
      }
      return isBindingAnnotation;
   }

   /**
    * Check the assignability of one type to another, taking into account the
    * actual type arguements
    * 
    * @param rawType1 the raw type of the class to check
    * @param actualTypeArguments1 the actual type arguements to check, or an
    *           empty array if not a parameterized type
    * @param rawType2 the raw type of the class to check
    * @param actualTypeArguments2 the actual type arguements to check, or an
    *           empty array if not a parameterized type
    * @return
    */
   public static boolean isAssignableFrom(Class<?> rawType1, Type[] actualTypeArguments1, Class<?> rawType2, Type[] actualTypeArguments2)
   {
      return Types.boxedClass(rawType1).isAssignableFrom(Types.boxedClass(rawType2)) && isAssignableFrom(actualTypeArguments1, actualTypeArguments2);
   }

   public static boolean matches(Class<?> rawType1, Type[] actualTypeArguments1, Class<?> rawType2, Type[] actualTypeArguments2)
   {
      return Types.boxedClass(rawType1).equals(Types.boxedClass(rawType2)) && isAssignableFrom(actualTypeArguments1, actualTypeArguments2);
   }

   public static boolean isAssignableFrom(Type[] actualTypeArguments1, Type[] actualTypeArguments2)
   {
      for (int i = 0; i < actualTypeArguments1.length; i++)
      {
         Type type1 = actualTypeArguments1[i];
         Type type2 = Object.class;
         if (actualTypeArguments2.length > i)
         {
            type2 = actualTypeArguments2[i];
         }
         if (!isAssignableFrom(type1, type2))
         {
            return false;
         }
      }
      return true;
   }

   public static boolean isAssignableFrom(Type type1, Set<? extends Type> types2)
   {
      for (Type type2 : types2)
      {
         if (isAssignableFrom(type1, type2))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean matches(Type type1, Set<? extends Type> types2)
   {
      for (Type type2 : types2)
      {
         if (matches(type1, type2))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean isAssignableFrom(Type type1, Type[] types2)
   {
      for (Type type2 : types2)
      {
         if (isAssignableFrom(type1, type2))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean isAssignableFrom(Type type1, Type type2)
   {
      if (type1 instanceof Class)
      {
         Class<?> clazz = (Class<?>) type1;
         if (isAssignableFrom(clazz, EMPTY_TYPES, type2))
         {
            return true;
         }
      }
      if (type1 instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType1 = (ParameterizedType) type1;
         if (parameterizedType1.getRawType() instanceof Class)
         {
            if (isAssignableFrom((Class<?>) parameterizedType1.getRawType(), parameterizedType1.getActualTypeArguments(), type2))
            {
               return true;
            }
         }
      }
      if (type1 instanceof WildcardType)
      {
         WildcardType wildcardType = (WildcardType) type1;
         if (isTypeBounded(type2, wildcardType.getLowerBounds(), wildcardType.getUpperBounds()))
         {
            return true;
         }
      }
      if (type2 instanceof WildcardType)
      {
         WildcardType wildcardType = (WildcardType) type2;
         if (isTypeBounded(type1, wildcardType.getUpperBounds(), wildcardType.getLowerBounds()))
         {
            return true;
         }
      }
      if (type1 instanceof TypeVariable<?>)
      {
         TypeVariable<?> typeVariable = (TypeVariable<?>) type1;
         if (isTypeBounded(type2, EMPTY_TYPES, typeVariable.getBounds()))
         {
            return true;
         }
      }
      if (type2 instanceof TypeVariable<?>)
      {
         TypeVariable<?> typeVariable = (TypeVariable<?>) type2;
         if (isTypeBounded(type1, typeVariable.getBounds(), EMPTY_TYPES))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean matches(Type type1, Type type2)
   {
      if (type1 instanceof Class<?>)
      {
         Class<?> clazz = (Class<?>) type1;
         if (matches(clazz, EMPTY_TYPES, type2))
         {
            return true;
         }
      }
      if (type1 instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType1 = (ParameterizedType) type1;
         if (parameterizedType1.getRawType() instanceof Class)
         {
            if (matches((Class<?>) parameterizedType1.getRawType(), parameterizedType1.getActualTypeArguments(), type2))
            {
               return true;
            }
         }
      }
      if (type1 instanceof WildcardType)
      {
         WildcardType wildcardType = (WildcardType) type1;
         if (isTypeBounded(type2, wildcardType.getLowerBounds(), wildcardType.getUpperBounds()))
         {
            return true;
         }
      }
      if (type2 instanceof WildcardType)
      {
         WildcardType wildcardType = (WildcardType) type2;
         if (isTypeBounded(type1, wildcardType.getUpperBounds(), wildcardType.getLowerBounds()))
         {
            return true;
         }
      }
      if (type1 instanceof TypeVariable<?>)
      {
         TypeVariable<?> typeVariable = (TypeVariable<?>) type1;
         if (isTypeBounded(type2, EMPTY_TYPES, typeVariable.getBounds()))
         {
            return true;
         }
      }
      if (type2 instanceof TypeVariable<?>)
      {
         TypeVariable<?> typeVariable = (TypeVariable<?>) type2;
         if (isTypeBounded(type1, typeVariable.getBounds(), EMPTY_TYPES))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean isTypeBounded(Type type, Type[] lowerBounds, Type[] upperBounds)
   {
      if (lowerBounds.length > 0)
      {
         if (!isAssignableFrom(type, lowerBounds))
         {
            return false;
         }
      }
      if (upperBounds.length > 0)
      {
         if (!isAssignableFrom(upperBounds, type))
         {
            return false;
         }
      }
      return true;
   }

   public static boolean isAssignableFrom(Class<?> rawType1, Type[] actualTypeArguments1, Type type2)
   {
      if (type2 instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type2;
         if (parameterizedType.getRawType() instanceof Class)
         {
            if (isAssignableFrom(rawType1, actualTypeArguments1, (Class<?>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments()))
            {
               return true;
            }
         }
      }
      else if (type2 instanceof Class)
      {
         Class<?> clazz = (Class<?>) type2;
         if (isAssignableFrom(rawType1, actualTypeArguments1, clazz, EMPTY_TYPES))
         {
            return true;
         }
      }
      else if (type2 instanceof TypeVariable)
      {
         TypeVariable typeVariable = (TypeVariable) type2;
         if (isTypeBounded(rawType1, actualTypeArguments1, typeVariable.getBounds()))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean matches(Class<?> rawType1, Type[] actualTypeArguments1, Type type2)
   {
      if (type2 instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type2;
         if (parameterizedType.getRawType() instanceof Class<?>)
         {
            if (matches(rawType1, actualTypeArguments1, (Class<?>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments()))
            {
               return true;
            }
         }
      }
      else if (type2 instanceof Class<?>)
      {
         Class<?> clazz = (Class<?>) type2;
         if (matches(rawType1, actualTypeArguments1, clazz, EMPTY_TYPES))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Check the assiginability of a set of <b>flattened</b> types. This
    * algorithm will check whether any of the types1 matches a type in types2
    * 
    * @param types1
    * @param types2
    * @return
    */
   public static boolean isAssignableFrom(Set<Type> types1, Set<Type> types2)
   {
      for (Type type : types1)
      {
         if (isAssignableFrom(type, types2))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Check whether whether any of the types1 matches a type in types2
    * 
    * @param types1
    * @param types2
    * @return
    */
   public static boolean matches(Set<Type> types1, Set<Type> types2)
   {
      for (Type type : types1)
      {
         if (matches(type, types2))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Check the assiginability of a set of <b>flattened</b> types. This
    * algorithm will check whether any of the types1 matches a type in types2
    * 
    * @param types1
    * @param types2
    * @return
    */
   public static boolean isAssignableFrom(Set<Type> types1, Type type2)
   {
      for (Type type : types1)
      {
         if (isAssignableFrom(type, type2))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean isAssignableFrom(Type[] types1, Type type2)
   {
      for (Type type : types1)
      {
         if (isAssignableFrom(type, type2))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean isSerializable(Class<?> clazz)
   {
      return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
   }

}
