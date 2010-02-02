package org.jboss.weld.tests.extensions.interceptors;

import javax.interceptor.InvocationContext;

/**
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class LifecycleInterceptor
{
   static private boolean preDestroyCalled = false;
   static private boolean postConstructCalled = false;

   // PreDestroy
   public void preDestroy(InvocationContext ctx)
   {
      preDestroyCalled = true;
   }

   // @PostConstruct
   public void postConstruct(InvocationContext ctx)
   {
      Object marathon = ctx.getTarget();
      if (marathon instanceof Marathon)
      {
         Marathon m = (Marathon) marathon;
         m.setLength(42);
      }
      postConstructCalled = true;
   }

   static public boolean isPostConstructCalled()
   {
      return postConstructCalled;
   }

   static public boolean isPreDestroyCalled()
   {
      return preDestroyCalled;
   }

}
