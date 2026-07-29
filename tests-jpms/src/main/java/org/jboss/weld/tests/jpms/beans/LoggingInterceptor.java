package org.jboss.weld.tests.jpms.beans;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@Logged
public class LoggingInterceptor {

    public static boolean invoked = false;

    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {
        invoked = true;
        return ctx.proceed();
    }
}
