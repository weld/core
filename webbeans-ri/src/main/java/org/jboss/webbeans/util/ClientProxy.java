package org.jboss.webbeans.util;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;


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
   
}
