package org.jboss.weld.tests.extensions.interceptors;

import javax.interceptor.InvocationContext;

/**
 * Interceptor that adds one to the result of a method
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class IncrementingInterceptor
{
   private static boolean doAroundCalled = false;

   // @AroundInvoke
   public Object doAround(InvocationContext context) throws Exception
   {
      doAroundCalled = true;
      Integer res = (Integer)context.proceed();
      return res + 1;
   }
  
   public static boolean isDoAroundCalled()
   {
      return doAroundCalled;
   }
}
