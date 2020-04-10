package org.jboss.weld.tests.interceptors.visibility;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@PanelInterceptionBinding
@Interceptor
@Priority(10)
public class PanelInterceptor {

    public static boolean called = false;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        called = true;
        return ctx.proceed();
    }

}
