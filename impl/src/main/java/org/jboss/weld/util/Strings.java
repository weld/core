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

import java.beans.Introspector;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;


/**
 * String utilities
 * 
 * @author Pete Muir
 * 
 */
public class Strings
{

   /**
    * Decapitalizes a String
    * 
    * @param camelCase The String
    * @return The decapitalized result
    */
   public static String decapitalize(String camelCase)
   {
      return Introspector.decapitalize(camelCase);
   }

   /**
    * Split a string into parts
    * 
    * @param strings The sources
    * @param delims The delimeter
    * @return The parts
    */
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

   /**
    * Returns a textual representation of a map for debug purposes
    * 
    * @param header The description of the map
    * @param map The map
    * @return A textual representation
    */
   public static String mapToString(String header, Map<?, ?> map)
   {
      StringBuilder buffer = new StringBuilder();
      if (map == null)
      {
         buffer.append(header).append("null\n");
         return buffer.toString();
      }
      buffer.append(header).append("[").append(map.size()).append(" entries]\n");
      int i = 0;
      for (Entry<?, ?> entry: map.entrySet())
      {
         Object value = entry.getValue();
         buffer.append("  #").append(++i).append(": ").append(entry.getKey()).append(" -> ");
         if (value instanceof Iterable<?>)
         {
            buffer.append("\n");
            for (Object subValue : (Iterable<?>) value)
            {
               buffer.append("    ").append(subValue.toString()).append("\n");
            }
         }
         else
         {
            buffer.append(value.toString()).append("\n");
         }
      }
      return buffer.toString();
   }

   /**
    * Returns a textual representation of a collection for debug purposes
    * 
    * @param header The description of the collection
    * @param collection The collection
    * @return A textual representation
    */
   public static String collectionToString(String header, Collection<?> collection)
   {
      StringBuilder buffer = new StringBuilder();
      if (collection == null)
      {
         buffer.append(header + "null\n");
         return buffer.toString();
      }
      buffer.append(header + "[" + collection.size() + " entries]\n");
      int i = 0;
      for (Object item : collection)
      {
         buffer.append("  #" + ++i + ": " + item.toString() + "\n");
      }
      return buffer.toString();
   }

}
