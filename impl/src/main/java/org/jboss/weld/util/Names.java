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
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.weld.introspector.WeldCallable;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.util.reflection.Reflections;

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
   
   public static String toString(Type baseType)
   {
      return new StringBuilder().append(Reflections.getRawType(baseType).getSimpleName()).append(toString(Reflections.getActualTypeArguments(baseType))).toString();
      
   }
   
   public static String toString(WeldClass<?> clazz)
   {
      return new StringBuilder().append(modifiersToString(clazz.getJavaClass().getModifiers())).append(toString(clazz.getAnnotations())).append(" class ").append(clazz.getName()).append(toString(clazz.getActualTypeArguments())).toString().trim();
   }

   public static String toString(WeldField<?, ?> field)
   {
      return new StringBuilder().append("[field] ").append(addSpaceIfNeeded(toString(field.getAnnotations()))).append(addSpaceIfNeeded(modifiersToString(field.getJavaMember().getModifiers()))).append(field.getDeclaringType().getName()).append(".").append(field.getName()).toString().trim();
   }
   
   public static String toString(WeldCallable<?, ?, ? extends Member> callable)
   {
      if (callable instanceof WeldMethod<?, ?>)
      {
         return toString((WeldMethod<?, ?>) callable);
      }
      else if (callable instanceof WeldConstructor<?>)
      {
         return toString((WeldConstructor<?>) callable);
      }
      else
      {
         throw new IllegalArgumentException("Unable to produce a toString representation of classes of type " + callable.getClass().getName());
      }
   }
   
   public static String toString(WeldMethod<?, ?> method)
   {
      return new StringBuilder().append("[method] ").append(addSpaceIfNeeded(toString(method.getAnnotations()))).append(addSpaceIfNeeded(modifiersToString(method.getJavaMember().getModifiers()))).append(method.getDeclaringType().getName()).append(".").append(method.getName()).append(parametersToTypeString(method.getWeldParameters())).toString().trim();
   }
   
   public static String toString(WeldConstructor<?> constructor)
   {
      return new StringBuilder().append("[constructor] ").append(addSpaceIfNeeded(toString(constructor.getAnnotations()))).append(addSpaceIfNeeded(modifiersToString(constructor.getJavaMember().getModifiers()))).append(constructor.getDeclaringType().getName()).append(parametersToTypeString(constructor.getWeldParameters())).toString().trim();
   }
   
   private static String addSpaceIfNeeded(String string)
   {
      if (string.length() > 0)
      {
         return new StringBuilder().append(string).append(" ").toString();
      }
      else
      {
         return string;
      }
   }
   
   
   private static String parametersToTypeString(Iterable<? extends WeldParameter<?, ?>> parameters)
   {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("(");
      int i = 0;
      for (WeldParameter<?, ?> parameter : parameters)
      {
         if (i > 0)
         {
            stringBuilder.append(", ");
         }
         stringBuilder.append(toString(parameter.getBaseType()));
         i++;
      }
      return stringBuilder.append(")").toString();
   }

   public static String toString(WeldParameter<?, ?> parameter)
   {
      return new StringBuilder().append("[parameter ").append(parameter.getPosition() + 1).append("] of ").append(toString(parameter.getDeclaringWeldCallable())).toString();
   }

   private static String modifiersToString(int modifiers)
   {
      return iterableToString(parseModifiers(modifiers), " ");
   }

   private static String iterableToString(Iterable<?> items, String delimiter)
   {
      StringBuilder stringBuilder = new StringBuilder();
      int i = 0;
      for (Object item : items)
      {
         if (i > 0)
         {
            stringBuilder.append(delimiter);
         }
         stringBuilder.append(item);
         i++;
      }
      return stringBuilder.toString();
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

   public static String toString(Type[] actualTypeArguments)
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

   public static String toString(Iterable<Annotation> annotations)
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
   public static String toString(Annotation[] annotations)
   {
      return toString(Arrays.asList(annotations));
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
