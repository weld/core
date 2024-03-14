package org.jboss.weld.tests.proxy.ignoreinvalidmethods.inheritance;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Priority(1)
@Secure
@Interceptor
public class SecureInterceptor {

    public static int timesInvoked = 0;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        timesInvoked++;
        return context.proceed();
    }
}
