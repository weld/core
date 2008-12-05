package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
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
}
