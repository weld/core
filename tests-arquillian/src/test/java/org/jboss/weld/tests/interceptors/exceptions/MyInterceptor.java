package org.jboss.weld.tests.interceptors.exceptions;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 *
 */
@Interceptor
@FooBinding
public class MyInterceptor {

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext invocation) throws Exception {
        return invocation.proceed();
    }

}
