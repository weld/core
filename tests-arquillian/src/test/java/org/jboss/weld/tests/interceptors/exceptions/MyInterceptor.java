package org.jboss.weld.tests.interceptors.exceptions;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

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
