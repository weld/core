package org.jboss.weld.tests.interceptors.weld760;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 *
 */
@Interceptor
@MyInterceptorBinding
public class MyInterceptor {

    public static boolean isFired;

    @AroundInvoke
    public Object manage(final InvocationContext ctx) throws Exception {
        isFired = true;
        return ctx.proceed();
    }
}