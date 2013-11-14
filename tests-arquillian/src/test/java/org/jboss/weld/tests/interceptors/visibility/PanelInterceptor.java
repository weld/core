package org.jboss.weld.tests.interceptors.visibility;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

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
