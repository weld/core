package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Basic
public class BasicInterceptor {

    private static Object target;

    public static Object getTarget() {
        return target;
    }

    public static void reset() {
        target = null;
    }

    @AroundInvoke
    public Object classInterceptor(InvocationContext ctx) throws Exception {
        target = ctx.getTarget();
        return ctx.proceed();
    }

}
