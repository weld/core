package org.jboss.weld.environment.se.test.beandiscovery.priority;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

import javax.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

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
