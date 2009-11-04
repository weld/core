package org.jboss.weld.test.interceptors.passivation.broken.interceptorWithNonSerializableField;

import java.io.Serializable;

import javax.interceptor.Interceptor;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.inject.Inject;

/**
 * @author Marius Bogoevici
 */
@Interceptor @Pass
public class Defender implements Serializable
{

   @Inject
   Team team;

   @AroundInvoke
   public Object defend(InvocationContext invocationContext) throws Exception
   {
      return invocationContext.proceed();
   }

}