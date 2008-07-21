package org.jboss.webbeans.util;

import java.beans.Introspector;
import java.util.StringTokenizer;

public class Strings
{

   public static String decapitalize(String camelCase)
   {
      return Introspector.decapitalize(camelCase);
   }
 
   public static String[] split(String strings, String delims)
   {
      if (strings==null)
      {
         return new String[0];
      }
      else
      {      
         StringTokenizer tokens = new StringTokenizer(strings, delims);
         String[] result = new String[ tokens.countTokens() ];
         int i=0;
         while ( tokens.hasMoreTokens() )
         {
            result[i++] = tokens.nextToken();
         }
         return result;
      }
   }   
}
