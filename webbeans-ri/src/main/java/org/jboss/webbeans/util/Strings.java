package org.jboss.webbeans.util;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
      buffer.append(header + "[" + map.size() + " entries]\n");
      int i = 0;
      for (Object key : map.keySet())
      {
         Object value = map.get(key);
         buffer.append("  #" + ++i + ": " + key.toString() + "->");
         if (value instanceof Iterable)
         {
            buffer.append("\n");
            for (Object subValue : (Iterable<?>) value)
            {
                buffer.append("    " + subValue.toString() + "\n");
            }
         }
         else
         {
            buffer.append(value.toString() + "\n");
         }
      }
      return buffer.toString();
   }

   public static void main(String[] args) {
     Map map = new HashMap<String, Collection<?>>();
     Collection a = new ArrayList<String>();
     a.add("1"); a.add("2");
     map.put("foo", a);
     Collection b = new ArrayList<String>();
     b.add("3"); b.add("4");
     map.put("bar", b);
     System.out.println(mapToString("Header: ", map));
   }
   
}
