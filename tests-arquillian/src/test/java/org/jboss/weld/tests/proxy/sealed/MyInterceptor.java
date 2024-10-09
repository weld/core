package org.jboss.weld.tests.proxy.sealed;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@Priority(1)
@MyBinding
public class MyInterceptor {

    public static int timesInvoked = 0;

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {
        timesInvoked++;
        return ic.proceed();
    }
}
