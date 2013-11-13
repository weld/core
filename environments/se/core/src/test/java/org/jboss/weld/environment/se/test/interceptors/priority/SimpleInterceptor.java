package org.jboss.weld.environment.se.test.interceptors.priority;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@SimpleInterceptorBinding
@Priority(1000)
public class SimpleInterceptor {

    public static boolean intercepted = false;

    @AroundInvoke
    public Object recordMethodCall(InvocationContext ctx) throws Exception {
        intercepted = true;
        return ctx.proceed();
    }

}
