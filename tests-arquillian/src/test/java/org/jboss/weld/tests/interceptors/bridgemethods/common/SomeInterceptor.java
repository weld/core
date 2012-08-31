package org.jboss.weld.tests.interceptors.bridgemethods.common;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

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
