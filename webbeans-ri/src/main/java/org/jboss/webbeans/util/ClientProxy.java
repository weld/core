package org.jboss.webbeans.util;



public class ClientProxy
{
   
   public static boolean isProxyable(Class<?> rawType)
   {
      // TODO Add logging
      
      if (Reflections.getConstructor(rawType) == null)
      {
         return false;
      }
      else if (Reflections.isTypeOrAnyMethodFinal(rawType))
      {
         return false;
      }
      else if (Reflections.isPrimitive(rawType))
      {
         return false;
      }
      else if (Reflections.isArrayType(rawType))
      {
         return false;
      }
      else
      {
         return true;
      }
   }
   
   public static boolean isProxy(Object instance) {
      return instance.getClass().getName().indexOf("_$$_javassist_") > 0;
   }   
   
}
