package org.jboss.webbeans.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.jboss.webbeans.introspector.AnnotatedField;

public class Strings
{

   public static String decapitalize(String camelCase)
   {
      return Introspector.decapitalize(camelCase);
   }

   public static String[] split(String strings, String delims)
   {
      if (strings == null)
      {
         return new String[0];
      }
      else
      {
         StringTokenizer tokens = new StringTokenizer(strings, delims);
         String[] result = new String[tokens.countTokens()];
         int i = 0;
         while (tokens.hasMoreTokens())
         {
            result[i++] = tokens.nextToken();
         }
         return result;
      }
   }

   public static String mapToString(String header, Map<?, ?> map)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(header + map.size() + "\n");
      int i = 0;
      for (Object key : map.keySet())
      {
         Object value = map.get(key);
         if (value instanceof Iterable)
         {
            for (Object subValue : (Iterable<?>) value)
            {
               buffer.append(++i + " - " + key.toString() + ": " + subValue.toString() + "\n");
            }
         }
         else
         {
            buffer.append(++i + " - " + key.toString() + ": " + value.toString() + "\n");
         }
      }
      return buffer.toString();
   }

}
