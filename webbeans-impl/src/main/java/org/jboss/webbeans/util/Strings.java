package org.jboss.webbeans.util;

import java.beans.Introspector;

public class Strings
{

   public static String decapitalize(String camelCase)
   {
      return Introspector.decapitalize(camelCase);
   }
   
}
