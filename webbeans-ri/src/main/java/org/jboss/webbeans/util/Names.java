package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

   public static int count(Iterable<?> iterable)
   {
      int count = 0;
      for (Iterator<?> i = iterable.iterator(); i.hasNext();)
      {
         count++;
      }
      return count;
   }

}
