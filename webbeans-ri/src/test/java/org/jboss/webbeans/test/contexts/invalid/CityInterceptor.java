package org.jboss.webbeans.test.contexts.invalid;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.webbeans.Interceptor;

@CityBinding 
@Interceptor
public class CityInterceptor
{
   @AroundInvoke
   public Object manageTransaction(InvocationContext context) {
      return null;
   }
}
