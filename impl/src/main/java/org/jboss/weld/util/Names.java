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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to produce friendly names e.g. for debugging
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
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

   public static String classToString(Class<?> rawType, Set<Annotation> annotations, Type[] actualTypeArguments)
   {
      return new NamesStringBuilder("class").add(modifiersToString(rawType.getModifiers())).add(annotationsToString(annotations)).add(rawType.getName()).add(typesToString(actualTypeArguments)).toString();
   }

   public static String fieldToString(Field field, Set<Annotation> annotations, Type[] actualTypeArguments)
   {
      return new NamesStringBuilder("field in " + field.getDeclaringClass().getName()).add(modifiersToString(field.getModifiers())).add(annotationsToString(annotations)).add(field.getType().getName()).add(typesToString(actualTypeArguments)).add(field.getName()).toString();
   }

   public static String methodToString(Method method, Set<Annotation> annotations, Type[] actualTypeArguments, List<?> parameters)
   {
      return new NamesStringBuilder("method in " + method.getDeclaringClass().getName()).add(modifiersToString(method.getModifiers())).add(annotationsToString(annotations)).add(method.getName()).add(typesToString(actualTypeArguments)).add(parametersToString(parameters)).toString();
   }

   private static String parametersToString(List<?> parameters)
   {
      return "(" + iterableToString(parameters, ", ") + ")";
   }

   public static String constructorToString(Constructor<?> constructor, Set<Annotation> annotations, Type[] actualTypeArguments, List<?> parameters)
   {
      return new NamesStringBuilder("constructor for " + constructor.getDeclaringClass().getName()).add(modifiersToString(constructor.getModifiers())).add(annotationsToString(annotations)).add(constructor.getName()).add(typesToString(actualTypeArguments)).add(parametersToString(parameters)).toString();
   }

   public static String parameterToString(int position, Member member, Class<?> rawType, Set<Annotation> annotations, Type[] actualTypeArguments)
   {
      return new NamesStringBuilder().add("parameter " + position + " on " + member + "; " + modifiersToString(rawType.getModifiers())).add(annotationsToString(annotations)).add(rawType.getName()).add(typesToString(actualTypeArguments)).toString();
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

   private static String modifiersToString(int modifiers)
   {
      return iterableToString(parseModifiers(modifiers), " ");
   }

   private static String iterableToString(Iterable<?> items, String delimiter)
   {
      StringBuffer stringBuffer = new StringBuffer();
      int i = 0;
      for (Object item : items)
      {
         if (i > 0)
         {
            stringBuffer.append(delimiter);
         }
         stringBuffer.append(item);
         i++;
      }
      return stringBuffer.toString();
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

   public static String typesToString(Type[] actualTypeArguments)
   {
      if (actualTypeArguments.length == 0)
      {
         return "";
      }
      StringBuilder buffer = new StringBuilder();
      int i = 0;
      buffer.append("<");
      for (Type type : actualTypeArguments)
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
      buffer.append(">");
      return buffer.toString();
   }

   public static String annotationsToString(Iterable<Annotation> annotations)
   {
      StringBuilder builder = new StringBuilder();
      for (Annotation annotation : annotations)
      {
         builder.append(" ");
         builder.append("@").append(annotation.annotationType().getSimpleName());
      }
      return builder.toString().trim();
   }

   /**
    * Gets a string representation from an array of annotations
    * 
    * @param annotations The annotations
    * @return The string representation
    */
   public static String annotationsToString(Annotation[] annotations)
   {
      return annotationsToString(Arrays.asList(annotations));
   }

   public static String version(Package pkg)
   {
      if (pkg == null)
      {
         throw new IllegalArgumentException("Package can not be null");
      }
      else
      {
         return version(pkg.getImplementationVersion());
      }
   }

   public static String version(String version)
   {
      if (version != null)
      {
         StringBuilder builder = new StringBuilder();
         if (version.indexOf(".") > 0)
         {
            builder.append(version.substring(0, version.indexOf("."))).append(".");
            version = version.substring(version.indexOf(".") + 1);
         }
         if (version.indexOf(".") > 0)
         {
            builder.append(version.substring(0, version.indexOf("."))).append(".");
            version = version.substring(version.indexOf(".") + 1);
         }
         if (version.indexOf("-") > 0)
         {
            builder.append(version.substring(0, version.indexOf("-"))).append(" (");
            builder.append(version.substring(version.indexOf("-") + 1)).append(")");
         }
         else if (version.indexOf(".") > 0)
         {
            builder.append(version.substring(0, version.indexOf("."))).append(" (");
            builder.append(version.substring(version.indexOf(".") + 1)).append(")");
         }
         else
         {
            builder.append(version);
         }
         return builder.toString();
      }
      return "SNAPSHOT";
   }

}
