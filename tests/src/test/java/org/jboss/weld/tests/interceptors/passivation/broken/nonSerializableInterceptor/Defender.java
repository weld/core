package org.jboss.weld.tests.interceptors.passivation.broken.nonSerializableInterceptor;

import javax.interceptor.Interceptor;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


/**
 * @author Marius Bogoevici
 */
@Interceptor @Pass
public class Defender
{
   @AroundInvoke
   public Object defend(InvocationContext invocationContext) throws Exception
   {
      return invocationContext.proceed();
   }
   
}
