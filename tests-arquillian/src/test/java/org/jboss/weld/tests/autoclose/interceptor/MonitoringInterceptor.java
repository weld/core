package org.jboss.weld.tests.autoclose.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.test.util.ActionSequence;

@Interceptor
@Monitored
@Priority(1000)
public class MonitoringInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        ActionSequence.addAction("interceptor.aroundInvoke");
        return context.proceed();
    }
}
