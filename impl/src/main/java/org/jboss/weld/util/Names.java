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
   
   public static String toString(Class<?> rawType, Set<Annotation> annotations, Type[] actualTypeArguments)
   {
      if (actualTypeArguments.length > 0)
      {
         return new StringBuilder().append(Names.annotationsToString(annotations)).append(" ").append(rawType.getName()).append("<").append(Arrays.asList(actualTypeArguments)).append(">").toString();
      }
      else
      {
         return new StringBuilder().append(Names.annotationsToString(annotations)).append(" ").append(rawType.getName()).toString();
      }
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
   
   public static String annotationsToString(Iterable<Annotation> annotations)
   {
      StringBuilder builder = new StringBuilder();
      int i = 0;
      for (Annotation annotation : annotations)
      {
         if (i > 0)
         {
            builder.append(" ");
         }
         builder.append("@").append(annotation.annotationType().getSimpleName());
         i++;
      }
      return builder.toString();
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
