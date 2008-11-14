package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;

import javax.webbeans.manager.Context;

public class Contexts 
{
   public static final ThreadLocal<Context> sessionContext = new ThreadLocal<Context>();
   public static final ThreadLocal<Context> requestContext = new ThreadLocal<Context>();
   public static final ThreadLocal<Context> applicationContext = new ThreadLocal<Context>();

   public Context getSessionContext() {
      return sessionContext.get();
   }
   
   public Context getRequestContext() {
      return requestContext.get();
   }

   public Context getApplicationContext() {
      return applicationContext.get();
   }

   public static void destroyContext(Class<? extends Annotation> scopeType)
   {
      // TODO: destroy the context;
   }

}
