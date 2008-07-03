package org.jboss.webbeans.util;

public class JNDI
{

   public static Object lookup(String name)
   {
      return lookup(name, Object.class);
   }
   
   public static <T> T lookup(String name, Class<? extends T> expectedType)
   {
      return null;
   }
   
}
