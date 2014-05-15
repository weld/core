package org.jboss.weld.tests.interceptors.visibility;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@PanelInterceptionBinding
@Interceptor
public class PanelInterceptor {

    public static boolean called = false;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        called = true;
        return ctx.proceed();
    }

}
