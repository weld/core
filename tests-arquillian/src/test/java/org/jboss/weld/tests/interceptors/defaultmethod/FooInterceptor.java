package org.jboss.weld.tests.interceptors.defaultmethod;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@FooInterceptorBinding
@Interceptor
public class FooInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        return true;
    }
}
