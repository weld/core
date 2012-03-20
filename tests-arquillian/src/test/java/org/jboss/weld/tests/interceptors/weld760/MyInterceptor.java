package org.jboss.weld.tests.interceptors.weld760;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

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