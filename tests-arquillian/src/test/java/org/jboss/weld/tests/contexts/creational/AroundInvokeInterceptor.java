package org.jboss.weld.tests.contexts.creational;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@AroundInvokeBinding
@Priority(1)
@Interceptor
public class AroundInvokeInterceptor {

    public static boolean aroundInvokeTriggered = false;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        aroundInvokeTriggered = true;
        return ctx.proceed();
    }

    public static void reset() {
        aroundInvokeTriggered = false;
    }
}
