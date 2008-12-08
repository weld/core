package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.webbeans.Observes;

import org.jboss.webbeans.ejb.EjbMetaData;

/**
 * Utility class to produce friendly names e.g. for debugging
 * 
 * @author Pete Muir
 * 
 */
public class Names
{

   private static Pattern CAPITAL_LETTERS = Pattern.compile("\\p{Upper}{1}\\p{Lower}*");

   public static String scopeTypeToString(Class<? extends Annotation> scopeType)
   {
      String scopeName = scopeType.getSimpleName();
      Matcher matcher = CAPITAL_LETTERS.matcher(scopeName);
      StringBuilder result = new StringBuilder();
      while (matcher.find())
      {
         result.append(matcher.group().toLowerCase() + " ");
      }
      return result.toString();
   }

   public static String ejbTypeFromMetaData(EjbMetaData<?> ejbMetaData)
   {
      if (ejbMetaData.isMessageDriven())
      {
         return "message driven";
      }
      else if (ejbMetaData.isSingleton())
      {
         return "singleton";
      }
      else if (ejbMetaData.isStateful())
      {
         return "stateful";
      }
      else if (ejbMetaData.isStateless())
      {
         return "stateless";
      }
      return "unknown";
   }

   public static int count(@Observes final Iterable<?> iterable)
   {
      int count = 0;
      for (Iterator<?> i = iterable.iterator(); i.hasNext();)
      {
         count++;
      }
      return count;
   }

   private static String list2String(List<String> list, String delimiter)
   {
      StringBuilder buffer = new StringBuilder();
      for (String item : list)
      {
         buffer.append(item);
         buffer.append(delimiter);
      }
      return buffer.toString();
   }

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

   private static String annotations2String(Annotation[] annotations)
   {
      StringBuilder buffer = new StringBuilder();
      for (Annotation annotation : annotations)
      {
         buffer.append("@" + annotation.annotationType().getSimpleName());
         buffer.append(" ");
      }
      return buffer.toString();
   }

   public static String field2String(Field field)
   {
      return "  Field " + 
         annotations2String(field.getAnnotations()) + 
         list2String(parseModifiers(field.getModifiers()), " ") + 
         field.getName() + ";";
   }

   public static String method2String(Method method)
   {
      return "  Method " + 
         method.getReturnType().getSimpleName() + " " + 
         annotations2String(method.getAnnotations()) + 
         list2String(parseModifiers(method.getModifiers()), " ") + 
         method.getName() + "(" + 
         parameters2String(method.getParameterTypes(), method.getParameterAnnotations()) + 
         ");";
   }
   
   public static String annotation2String(Annotation annotation)
   {
      return "Annotation " + 
         annotations2String(annotation.annotationType().getAnnotations()) + 
         annotation.annotationType().getSimpleName();
   }
   
   public static String constructor2String(Constructor<?> method)
   {
      return "  Constructor " + 
         annotations2String(method.getAnnotations()) + 
         list2String(parseModifiers(method.getModifiers()), " ") + 
         method.getName() + "(" + 
         parameters2String(method.getParameterTypes(), method.getParameterAnnotations()) + 
         ");";
   }

   private static String parameters2String(Class<?>[] parameterTypes, Annotation[][] annotations)
   {
      StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < parameterTypes.length; i++)
      {
         if (i > 0)
         {
            buffer.append(", ");
         }
         buffer.append(annotations2String(annotations[i]) + type2String(parameterTypes[i]));
      }
      return buffer.toString();
   }

   public static String type2String(Class<?> clazz)
   {
      return annotations2String(clazz.getAnnotations()) + clazz.getName();
   }
   
   public static String class2String(Class<?> clazz) {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Class " + type2String(clazz));
      buffer.append("\n");
      for (Field field : clazz.getFields())
      {
         buffer.append(field2String(field));
         buffer.append("\n");
      }
      buffer.append("\n");
      for (Method method : clazz.getMethods())
      {
         buffer.append(method2String(method));
         buffer.append("\n");
      }      
      return buffer.toString();
   }

}
