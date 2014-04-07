package org.jboss.weld.environment.se.test.beandiscovery.priority;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Priority(APPLICATION)
@Interceptor
@Normalized
public class NormalizingInterceptor {

    public static int invocations = 0;

    @AroundInvoke
    public Object equalize(InvocationContext ctx) throws Exception {
        invocations++;
        return ctx.proceed();
    }

    static void reset() {
        invocations = 0;
    }

}
