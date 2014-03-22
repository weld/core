package org.jboss.weld.environment.se.test.isolation;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Zoom
public class ZoomInterceptor {

    public static int invocations = 0;

    @AroundInvoke
    public Object doZoom(InvocationContext ctx) throws Exception {
        invocations++;
        return ctx.proceed();
    }

}
