package org.jboss.weld.tests.interceptors.bridgemethods.common;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 *
 */
@Interceptor
@SomeInterceptorBinding
public class SomeInterceptor {

    public static int invocationCount;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        invocationCount++;
        return invocationContext.proceed();
    }
}
