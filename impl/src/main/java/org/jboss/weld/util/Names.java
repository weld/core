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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to produce friendly names e.g. for debugging
 * 
 * @author Pete Muir
 * 
 */
public class Names
{
   // Pattern for recognizing strings with leading capital letter
   private static Pattern CAPITAL_LETTERS = Pattern.compile("\\p{Upper}{1}\\p{Lower}*");

   /**
    * Gets a string representation of the scope type annotation
    * 
    * @param scopeType The scope type
    * @return A string representation
    */
   public static String scopeTypeToString(Class<? extends Annotation> scopeType)
   {
      StringBuilder result = new StringBuilder();
      if (scopeType != null)
      {
         String scopeName = scopeType.getSimpleName();
         Matcher matcher = CAPITAL_LETTERS.matcher(scopeName);
         int i = 0;
         while (matcher.find())
         {
            String name = matcher.group();
            if (i > 0)
            {
               name = name.toLowerCase();
            }
            result.append(name).append(" ");
            i++;
         }
      }
      return result.toString();
   }

   /**
    * Counts item in an iteratble
    * 
    * @param iterable The iteraboe
    * @return The count
    */
   public static int count(final Iterable<?> iterable)
   {
      int count = 0;
      for (Iterator<?> i = iterable.iterator(); i.hasNext();)
      {
         count++;
      }
      return count;
   }

   /**
    * Converts a list of strings to a String with given delimeter
    * 
    * @param list The list
    * @param delimiter The delimeter
    * @return The string representation
    */
   private static String listToString(List<String> list, String delimiter)
   {
      StringBuilder buffer = new StringBuilder();
      for (String item : list)
      {
         buffer.append(item);
         buffer.append(delimiter);
      }
      return buffer.toString();
   }

   /**
    * Parses a reflection modifier to a list of string
    * 
    * @param modifier The modifier to parse
    * @return The resulting string list
    */
   private static List<String> parseModifiers(int modifier)
   {
      List<String> modifiers = new ArrayList<String>();
      if (Modifier.isPrivate(modifier))
      {
         modifiers.add("private");
      }
      if (Modifier.isProtected(modifier))
      {
         modifiers.add("protected");
      }
      if (Modifier.isPublic(modifier))
      {
         modifiers.add("public");
      }
      if (Modifier.isAbstract(modifier))
      {
         modifiers.add("abstract");
      }
      if (Modifier.isFinal(modifier))
      {
         modifiers.add("final");
      }
      if (Modifier.isNative(modifier))
      {
         modifiers.add("native");
      }
      if (Modifier.isStatic(modifier))
      {
         modifiers.add("static");
      }
      if (Modifier.isStrict(modifier))
      {
         modifiers.add("strict");
      }
      if (Modifier.isSynchronized(modifier))
      {
         modifiers.add("synchronized");
      }
      if (Modifier.isTransient(modifier))
      {
         modifiers.add("transient");
      }
      if (Modifier.isVolatile(modifier))
      {
         modifiers.add("volatile");
      }
      if (Modifier.isInterface(modifier))
      {
         modifiers.add("interface");
      }
      return modifiers;
   }

   /**
    * Gets a string representation from an array of annotations
    * 
    * @param annotations The annotations
    * @return The string representation
    */
   private static String annotationsToString(Annotation[] annotations)
   {
      StringBuilder buffer = new StringBuilder();
      for (Annotation annotation : annotations)
      {
         buffer.append("@" + annotation.annotationType().getSimpleName());
         buffer.append(" ");
      }
      return buffer.toString();
   }

   /**
    * Gets a string representation from a field
    * 
    * @param field The field
    * @return The string representation
    */
   public static String fieldToString(Field field)
   {
      if (!field.isAccessible())
      {
         field.setAccessible(true);
      }
      return "  Field " + annotationsToString(field.getAnnotations()) + listToString(parseModifiers(field.getModifiers()), " ") + field.getName();
   }

   /**
    * Gets the string representation from a method
    * 
    * @param method The method
    * @return The string representation
    */
   public static String methodToString(Method method)
   {
      if (!method.isAccessible())
      {
         method.setAccessible(true);
      }
      return "  Method " + method.getReturnType().getSimpleName() + " " + annotationsToString(method.getAnnotations()) + listToString(parseModifiers(method.getModifiers()), " ") + method.getName() + "(" + parametersToString(method.getParameterTypes(), method.getParameterAnnotations(), false) + ");\n";
   }

   /**
    * Gets a string representation from an annotation
    * 
    * @param annotation The annotation
    * @return The string representation
    */
   public static String annotationToString(Annotation annotation)
   {
      return "Annotation " + annotationsToString(annotation.annotationType().getAnnotations()) + annotation.annotationType().getSimpleName();
   }

   /**
    * Gets a string representation from a method
    * 
    * @param constructor The method
    * @return The string representation
    */
   public static String constructorToString(Constructor<?> constructor)
   {
      return "  Constructor " + annotationsToString(constructor.getAnnotations()) + listToString(parseModifiers(constructor.getModifiers()), " ") + constructor.getDeclaringClass().getSimpleName() + "(" + parametersToString(constructor.getParameterTypes(), constructor.getParameterAnnotations(), true) + ");\n";
   }

   /**
    * Gets a string representation from a list of parameters and their
    * annotations
    * 
    * @param parameterTypes The parameters
    * @param annotations The annotation map
    * @return The string representation
    */
   private static String parametersToString(Class<?>[] parameterTypes, Annotation[][] annotations, boolean constructor)
   {
      StringBuilder buffer = new StringBuilder();
      int start = constructor ? 1 : 0;
      for (int i = start; i < parameterTypes.length; i++)
      {
         if (i > start)
         {
            buffer.append(", ");
         }
         buffer.append(annotationsToString(annotations[i]) + typeToString(parameterTypes[i]));
      }
      return buffer.toString();
   }

   /**
    * Gets a string representation from a type
    * 
    * @param clazz The type
    * @return The string representation
    */
   private static String typeToString(Class<?> clazz)
   {
      return annotationsToString(clazz.getAnnotations()) + clazz.getSimpleName();
   }

   /**
    * Gets a string representation from a class
    * 
    * @param clazz The class
    * @return The string representation
    */
   public static String classToString(Class<?> clazz)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Class " + typeToString(clazz) + "\n");
      for (Field field : clazz.getFields())
      {
         buffer.append(fieldToString(field));
      }
      for (Constructor<?> constructor : clazz.getConstructors())
      {
         buffer.append(constructorToString(constructor));
      }
      for (Method method : clazz.getMethods())
      {
         buffer.append(methodToString(method));
      }
      return buffer.toString();
   }
   
   public static String typesToString(Set<? extends Type> types)
   {
      StringBuilder buffer = new StringBuilder();
      int i = 0;
      buffer.append("[");
      for (Type type : types)
      {
         if (i > 0)
         {
            buffer.append(", ");
         }
         if (type instanceof Class<?>)
         {
            buffer.append(((Class<?>) type).getSimpleName());
         }
         else
         {
            buffer.append(type.toString());
         }
         i++;
      }
      buffer.append("]");
      return buffer.toString();
   }
   
   public static String annotationsToString(Set<Annotation> annotations)
   {
      StringBuilder buffer = new StringBuilder();
      int i = 0;
      buffer.append("[");
      for (Annotation annotation : annotations)
      {
         if (i > 0)
         {
            buffer.append(", ");
         }
         buffer.append("@").append(annotation.annotationType().getSimpleName());
         i++;
      }
      buffer.append("]");
      return buffer.toString();
   }
   
   public static String version(Package pkg)
   {
      if (pkg != null)
      {
         String version = pkg.getImplementationVersion();
         if (version != null)
         {
            return new StringBuilder().append(version.substring(0, version.lastIndexOf("-"))).append(" (").append(version.substring(version.lastIndexOf("-") + 1)).append(")").toString();
         }
      }
      return "SNAPSHOT";
   }

}
