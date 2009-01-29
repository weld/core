package org.jboss.webbeans.injection;

import java.lang.reflect.InvocationTargetException;

import javax.inject.CreationException;
import javax.inject.ExecutionException;

class Exceptions
{
   
   private static void rethrowException(Throwable t, Class<? extends RuntimeException> exceptionToThrow)
   {
      if (t instanceof RuntimeException)
      {
         throw (RuntimeException) t;
      }
      else
      {
         RuntimeException e;
         try
         {
            e = exceptionToThrow.newInstance();
         }
         catch (InstantiationException e1)
         {
            throw new ExecutionException(e1);
         }
         catch (IllegalAccessException e1)
         {
            throw new ExecutionException(e1);
         }
         e.initCause(t);
         throw e;
      }
   }
   
   private static void rethrowException(Throwable t)
   {
      rethrowException(t, CreationException.class);
   }
   
   public static void rethrowException(IllegalArgumentException e)
   {
       rethrowException(e.getCause());
   }
   
   public static void rethrowException(IllegalArgumentException e, Class<? extends RuntimeException> exceptionToThrow)
   {
       rethrowException(e.getCause(), exceptionToThrow);
   }
   
   public static void rethrowException(InstantiationException e, Class<? extends RuntimeException> exceptionToThrow)
   {
       rethrowException(e.getCause(), exceptionToThrow);
   }
   
   public static void rethrowException(InstantiationException e)
   {
       rethrowException(e.getCause());
   }
   
   public static void rethrowException(IllegalAccessException e)
   {
       rethrowException(e.getCause());
   }
   
   public static void rethrowException(IllegalAccessException e, Class<? extends RuntimeException> exceptionToThrow)
   {
       rethrowException(e.getCause(), exceptionToThrow);
   }
   
   public static void rethrowException(InvocationTargetException e, Class<? extends RuntimeException> exceptionToThrow)
   {
       rethrowException(e.getCause(), exceptionToThrow);
   }
   
   public static void rethrowException(InvocationTargetException e)
   {
       rethrowException(e.getCause());
   }
   
}
